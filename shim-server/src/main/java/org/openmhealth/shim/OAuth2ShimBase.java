/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;


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
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.SerializationUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Common code for all OAuth2.0 based shims.
 *
 * @author Danilo Bonilla
 */
public abstract class OAuth2ShimBase extends ShimBase implements OAuth2Shim {

    private AuthorizationRequestParametersRepo authorizationRequestParametersRepo;

    private AccessParametersRepo accessParametersRepo;

    protected ShimServerConfig shimServerConfig;

    protected OAuth2ShimBase(ApplicationAccessParametersRepo applicationParametersRepo,
                             AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                             AccessParametersRepo accessParametersRepo,
                             ShimServerConfig shimServerConfig) {
        super(applicationParametersRepo);
        this.authorizationRequestParametersRepo = authorizationRequestParametersRepo;
        this.accessParametersRepo = accessParametersRepo;
        this.shimServerConfig = shimServerConfig;
    }

    protected abstract AuthorizationRequestParameters getAuthorizationRequestParameters(
        final String username, final UserRedirectRequiredException exception);

    protected abstract ResponseEntity<ShimDataResponse> getData(
        OAuth2RestOperations restTemplate, ShimDataRequest shimDataRequest) throws ShimException;

    protected String getCallbackUrl() {
        return shimServerConfig.getCallbackUrl(getShimKey());
    }

    @Override
    public AuthorizationRequestParameters getAuthorizationRequestParameters(String username,
                                                                            Map<String, String> addlParameters)
        throws ShimException {
        OAuth2RestOperations restTemplate = restTemplate();
        try {
            trigger(restTemplate, getTriggerDataRequest());
            return AuthorizationRequestParameters.authorized();
        } catch (UserRedirectRequiredException e) {
            /**
             * If an exception was thrown it means a redirect is required
             * for user's external authorization with toolmaker.
             */
            AccessTokenRequest accessTokenRequest =
                restTemplate.getOAuth2ClientContext().getAccessTokenRequest();
            String stateKey = accessTokenRequest.getStateKey();

            /**
             * Build an authorization request from the exception
             * parameters. We also serialize spring's accessTokenRequest.
             */
            AuthorizationRequestParameters authRequestParams =
                getAuthorizationRequestParameters(username, e);

            authRequestParams.setSerializedRequest(SerializationUtils.serialize(accessTokenRequest));
            authRequestParams.setStateKey(stateKey);

            authorizationRequestParametersRepo.save(authRequestParams);
            return authRequestParams;
        }
    }

    public OAuth2ProtectedResourceDetails getResource() {
        AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();
        resource.setAccessTokenUri(getBaseTokenUrl());
        resource.setUserAuthorizationUri(getBaseAuthorizeUrl());
        resource.setClientId(getClientId());
        resource.setScope(getScopes());
        resource.setClientSecret(getClientSecret());
        resource.setTokenName("access_token");
        resource.setGrantType("authorization_code");
        resource.setUseCurrentUri(true);
        return resource;
    }

    @Override
    public AuthorizationResponse handleAuthorizationResponse(HttpServletRequest servletRequest)
        throws ShimException {

        String state = servletRequest.getParameter("state");
        String code = servletRequest.getParameter("code");

        AuthorizationRequestParameters authorizationRequestParameters =
            authorizationRequestParametersRepo.findByStateKey(state);

        if (authorizationRequestParameters == null) {
            throw new IllegalStateException("Could not find corresponding authorization " +
                "request parameters, cannot continue.");
        }

        OAuth2RestOperations restTemplate = restTemplate(state, code);
        try {
            /**
             * Create a persistable access parameters entity so that
             * spring oauth2's client token services can relate
             * the serialized OAuth2AccessToken to it.
             */
            AccessParameters accessParameters = new AccessParameters();
            accessParameters.setUsername(authorizationRequestParameters.getUsername());
            accessParameters.setShimKey(getShimKey());
            accessParameters.setStateKey(state);
            accessParametersRepo.save(accessParameters);

            trigger(restTemplate, getTriggerDataRequest());

            /**
             * By this line we will have an approved access token or
             * not, if we do not then we delete the access parameters entity.
             */
            if (restTemplate.getAccessToken() == null) {
                accessParametersRepo.delete(accessParameters);
                return AuthorizationResponse.error("Did not receive approval");
            } else {
                accessParameters = accessParametersRepo.findByUsernameAndShimKey(
                    authorizationRequestParameters.getUsername(),
                    getShimKey(), new Sort(Sort.Direction.DESC, "dateCreated"));
            }
            return AuthorizationResponse.authorized(accessParameters);
        } catch (OAuth2Exception e) {
            //TODO: OAuth2Exception may include other stuff
            System.out.println("Problem trying out the token!");
            e.printStackTrace();
            return AuthorizationResponse.error(e.getMessage());
        }
    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {
        return getData(restTemplate(), shimDataRequest).getBody();
    }

    public void trigger(OAuth2RestOperations restTemplate, ShimDataRequest shimDataRequest) throws ShimException {
        getData(restTemplate, shimDataRequest);
    }

    protected OAuth2RestOperations restTemplate(String stateKey, String code) {

        DefaultAccessTokenRequest existingRequest =

            stateKey != null
                && authorizationRequestParametersRepo.findByStateKey(stateKey) != null ?

                (DefaultAccessTokenRequest) SerializationUtils.deserialize(
                    authorizationRequestParametersRepo.findByStateKey(stateKey).getSerializedRequest()) : null;

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
        AccessTokenProviderChain tokenProviderChain =
            new AccessTokenProviderChain(new ArrayList<>(
                Arrays.asList(getAuthorizationCodeAccessTokenProvider())));
        tokenProviderChain.setClientTokenServices(
            new AccessParameterClientTokenServices(accessParametersRepo));
        restTemplate.setAccessTokenProvider(tokenProviderChain);
        return restTemplate;
    }

    protected OAuth2RestOperations restTemplate() {
        return restTemplate(null, null);
    }
}
