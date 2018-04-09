/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmhealth.shim;

import org.openmhealth.shim.common.mapper.JsonNodeMappingException;
import org.openmhealth.shimmer.configuration.DeploymentSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.SerializationUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.openmhealth.shim.AuthorizationResponse.denied;
import static org.openmhealth.shim.AuthorizationResponse.error;
import static org.openmhealth.shim.OAuth2ErrorResponseCode.ACCESS_DENIED;


/**
 * @author Danilo Bonilla
 * @author Emerson Farrugia
 */
public abstract class OAuth2Shim implements Shim {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Shim.class);

    @Autowired
    private AuthorizationRequestParametersRepo authorizationRequestParametersRepo;

    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    protected DeploymentSettings deploymentSettings;

    public static final String REDIRECT_URL_KEY = "redirect_url";

    protected abstract OAuth2ClientSettings getClientSettings();

    @Override
    public boolean isConfigured() {

        return getClientSettings().hasClientId();
    }

    protected abstract ResponseEntity<ShimDataResponse> getData(
            OAuth2RestOperations restTemplate, ShimDataRequest shimDataRequest) throws ShimException;

    protected String getDefaultRedirectUrl() {

        return deploymentSettings.getRedirectUrl(getShimKey());
    }

    protected abstract AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider();

    @Override
    public AuthorizationRequestParameters getAuthorizationRequestParameters(
            String username,
            Map<String, String> additionalParameters)
            throws ShimException {

        OAuth2RestOperations restTemplate = restTemplate();

        try {
            // TODO replace with restTemplate.getAccessToken();
            trigger(restTemplate, getTriggerDataRequest());

            // if no exception has been thrown, assume that the current authorization is valid
            return AuthorizationRequestParameters.authorized();
        }
        catch (UserRedirectRequiredException e) {
            // if an exception was thrown it means a redirect is required
            AccessTokenRequest accessTokenRequest = restTemplate.getOAuth2ClientContext().getAccessTokenRequest();

            String stateKey = accessTokenRequest.getStateKey();

            /**
             * Build an authorization request from the exception
             * parameters. We also serialize spring's accessTokenRequest.
             */
            AuthorizationRequestParameters authRequestParams = new AuthorizationRequestParameters();
            authRequestParams.setRedirectUri(e.getRedirectUri());
            authRequestParams.setStateKey(e.getStateKey());
            authRequestParams.setAuthorizationUrl(getAuthorizationUrl(e, additionalParameters));
            authRequestParams.setSerializedRequest(SerializationUtils.serialize(accessTokenRequest));
            authRequestParams.setStateKey(stateKey);
            authRequestParams.setRequestParams(additionalParameters);
            // the url to custom page.
            authRequestParams.setClientRedirectUrl(deploymentSettings.getClientRedirectUrl());
            return authorizationRequestParametersRepo.save(authRequestParams);
        }
    }

    // TODO rename additional parameters, it's not clear what they're for
    protected abstract String getAuthorizationUrl(UserRedirectRequiredException exception,
            Map<String, String> addlParameters);


    public OAuth2ProtectedResourceDetails getResource() {

        AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();

        resource.setAccessTokenUri(getAccessTokenUrl());
        resource.setUserAuthorizationUri(getUserAuthorizationUrl());
        resource.setClientId(getClientSettings().getClientId());
        resource.setScope(getClientSettings().getScopes());
        resource.setClientSecret(getClientSettings().getClientSecret());
        resource.setUseCurrentUri(true);

        return resource;
    }

    /**
     * Request parameters to be used when 'triggering'
     * spring oauth2. This should be the equivalent
     * of a ping to the external data provider.
     *
     * @return - The Shim data request to use for trigger.
     */
    protected ShimDataRequest getTriggerDataRequest() {

        ShimDataRequest shimDataRequest = new ShimDataRequest();
        shimDataRequest.setDataTypeKey(getShimDataTypes()[0].toString());
        return shimDataRequest;
    }

    @Override
    public AuthorizationResponse processRedirect(HttpServletRequest servletRequest)
            throws ShimException {

        if (servletRequest.getParameter("error") != null) {
            OAuth2ErrorResponse errorResponse = getOAuth2ErrorResponse(servletRequest);

            if (errorResponse.getErrorCode() == ACCESS_DENIED) {
                return denied(errorResponse.getErrorDescription().orElse(null));
            }

            return error(errorResponse.getErrorDescription().orElse(errorResponse.getErrorCode().name()));
        }

        String stateKey = servletRequest.getParameter("state");

        AuthorizationRequestParameters authorizationRequestParameters =
                authorizationRequestParametersRepo.findByStateKey(stateKey);

        if (authorizationRequestParameters == null) {
            throw new IllegalStateException(
                    "Could not find corresponding authorization request parameters, cannot continue.");
        }

        String authorizationCode = servletRequest.getParameter("code");

        OAuth2RestOperations restTemplate = restTemplate(stateKey, authorizationCode);
        try {
            /**
             * Create a persistable access parameters entity so that
             * spring oauth2's client token services can relate
             * the serialized OAuth2AccessToken to it.
             */
            AccessParameters accessParameters = new AccessParameters();
            accessParameters.setUsername(authorizationRequestParameters.getUsername());
            accessParameters.setShimKey(getShimKey());
            accessParameters.setStateKey(stateKey);
            accessParametersRepo.save(accessParameters);

            try {
                trigger(restTemplate, getTriggerDataRequest());
            }
            catch (JsonNodeMappingException e) {
                // In this case authentication may have succeeded, but the data request may have failed so we
                // should not fail. We should check and see if authentication succeeded in subsequent lines.
            }

            /**
             * By this line we will have an approved access token or
             * not, if we do not then we delete the access parameters entity.
             */
            if (restTemplate.getAccessToken() == null) {
                accessParametersRepo.delete(accessParameters);
                return error("Did not receive approval");
            }
            else {
                accessParameters = accessParametersRepo.findByUsernameAndShimKey(
                        authorizationRequestParameters.getUsername(),
                        getShimKey(), new Sort(Sort.Direction.DESC, "dateCreated"));
            }
            return AuthorizationResponse.authorized(accessParameters);
        }
        catch (OAuth2Exception e) {
            //TODO: OAuth2Exception may include other stuff
            logger.error("Problem trying out the token", e);
            return error(e.getMessage());
        }
    }

    /**
     * @param servletRequest the redirect received from the data provider
     * @return the OAuth 2.0 error response
     */
    protected OAuth2ErrorResponse getOAuth2ErrorResponse(HttpServletRequest servletRequest) {

        return new OAuth2ErrorResponse(servletRequest);
    }


    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {

        return getData(restTemplate(), shimDataRequest).getBody();
    }

    // TODO rename this thing, if it's even necessary
    public void trigger(OAuth2RestOperations restTemplate, ShimDataRequest shimDataRequest) throws ShimException {

        getData(restTemplate, shimDataRequest);
    }

    protected OAuth2RestOperations restTemplate(String stateKey, String code) {

        DefaultAccessTokenRequest existingRequest = null;

        if (stateKey != null && authorizationRequestParametersRepo.findByStateKey(stateKey) != null) {
            existingRequest = SerializationUtils.deserialize(
                    authorizationRequestParametersRepo.findByStateKey(stateKey).getSerializedRequest());
        }

        if (existingRequest != null && code != null) {
            existingRequest.set("code", code);
        }

        DefaultOAuth2ClientContext context =
                new DefaultOAuth2ClientContext(existingRequest != null ?
                        existingRequest : new DefaultAccessTokenRequest());

        if (existingRequest != null) {
            context.setPreservedState(stateKey, "NONE");
        }

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(getResource(), context);

        // TODO figure out what's going on in here; is a chain the only way to set client token services?
        AccessTokenProviderChain tokenProviderChain =
                new AccessTokenProviderChain(singletonList(getAuthorizationCodeAccessTokenProvider()));
        tokenProviderChain.setClientTokenServices(
                new AccessParameterClientTokenServices(accessParametersRepo));
        restTemplate.setAccessTokenProvider(tokenProviderChain);

        restTemplate.setAuthenticator(new CaseStandardizingOAuth2RequestAuthenticator());

        return restTemplate;
    }

    protected OAuth2RestOperations restTemplate() {

        return restTemplate(null, null);
    }
}
