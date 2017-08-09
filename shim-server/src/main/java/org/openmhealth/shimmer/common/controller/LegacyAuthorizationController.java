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

package org.openmhealth.shimmer.common.controller;

import com.google.common.collect.Maps;
import org.openmhealth.shim.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.OAuth2Shim.REDIRECT_URL_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * @author Danilo Bonilla
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.openmhealth")
@EnableWebSecurity
@RestController
public class LegacyAuthorizationController {

    private static final Logger logger = LoggerFactory.getLogger(LegacyAuthorizationController.class);

    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    private AuthorizationRequestParametersRepo authorizationRequestParametersRepo;

    @Autowired
    private ShimRegistry shimRegistry;


    /**
     * Retrieve access parameters for the given username/fragment.
     *
     * @param username username fragment to search.
     * @return List of access parameters.
     */
    @RequestMapping(value = "authorizations", produces = APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> authorizations(@RequestParam(value = "username") String username)
            throws ShimException {

        List<AccessParameters> accessParameters = accessParametersRepo.findAllByUsernameLike(username);

        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Set<String>> auths = new HashMap<>();
        for (AccessParameters accessParameter : accessParameters) {
            if (!auths.containsKey(accessParameter.getUsername())) {
                auths.put(accessParameter.getUsername(), new HashSet<>());
            }
            auths.get(accessParameter.getUsername()).add(accessParameter.getShimKey());
        }
        for (final String uid : auths.keySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("username", uid);
            row.put("auths", auths.get(uid));
            results.add(row);
        }
        return results;
    }

    /**
     * Endpoint for triggering domain approval.
     *
     * @param username The user record for which we're authorizing a shim.
     * @param dataProviderRedirectUrl The URL to which the the data provider will redirect the user's browser after an
     * authorization
     * @param shim The shim registry key of the shim we're approving
     * @return AuthorizationRequest parameters, including a boolean flag if already authorized.
     */
    @RequestMapping(value = "/authorize/{shim}", produces = APPLICATION_JSON_VALUE)
    public AuthorizationRequestParameters initiateAuthorization(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "redirect_url", required = false) String dataProviderRedirectUrl,
            @PathVariable("shim") String shim) throws ShimException {

        logger.debug("Received /authorize/{} request with username {}", shim, username);

        setPassThroughAuthentication(username, shim);

        Map<String, String> additionalParameters = Maps.newHashMap();

        additionalParameters.put(REDIRECT_URL_KEY, dataProviderRedirectUrl);

        AuthorizationRequestParameters authorizationRequestParameters = shimRegistry
                .getShim(shim)
                .getAuthorizationRequestParameters(username, additionalParameters);

        authorizationRequestParameters.setUsername(username);

        return authorizationRequestParametersRepo.save(authorizationRequestParameters);
    }

    /**
     * Endpoint for removing authorizations for a given user and shim.
     *
     * @param username The user record for which we're removing shim access.
     * @param shim The shim registry key of the shim authorization we're removing.
     * @return Simple response message.
     */
    @RequestMapping(value = "/deauthorize/{shim}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removeAuthorization(
            @RequestParam(value = "username") String username,
            @PathVariable("shim") String shim)
            throws ShimException {

        // TODO revoke tokens from data provider
        List<AccessParameters> accessParameters = accessParametersRepo.findAllByUsernameAndShimKey(username, shim);

        accessParametersRepo.delete(accessParameters);

        return ok().build();
    }

    /**
     * Endpoint for handling redirects from external data providers
     *
     * @param servletRequest Request posted by the external data provider.
     * @return AuthorizationResponse object with details and result: authorize, error, or denied.
     */
    // TODO harmonize handling of ShimException, returning correct status codes where relevant
    @RequestMapping(value = "/authorize/{shim}/callback", method = {POST, GET}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorizationResponse> processRedirect(
            @PathVariable("shim") String shimKey,
            @RequestParam("state") String stateKey,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse)
            throws ShimException {

        logger.debug("A redirect has been received for shim '{}' with state key '{}'.", shimKey, stateKey);

        AuthorizationRequestParameters authParams = authorizationRequestParametersRepo.findByStateKey(stateKey);

        if (authParams == null) {
            logger.warn(
                    "The redirect can't be processed because an authorization request with state key '{}' doesn't exist.",
                    stateKey);

            return badRequest().build();
        }

        setPassThroughAuthentication(authParams.getUsername(), shimKey);

        AuthorizationResponse response =
                shimRegistry
                        .getShim(shimKey)
                        .processRedirect(servletRequest);

        // TODO determine what the HTTP status code should be here
        if (response.getType() != AuthorizationResponse.Type.AUTHORIZED) {
            return ok(response);
        }

        /**
         * Save the access parameters to local repo.
         * They will be re-fetched via username and path parameters
         * for future requests.
         */
        checkNotNull(response);
        checkNotNull(response.getAccessParameters());
        checkNotNull(authParams);

        response.getAccessParameters().setUsername(authParams.getUsername());
        response.getAccessParameters().setShimKey(shimKey);
        response.setRequestParameters(authParams.getRequestParams());

        accessParametersRepo.save(response.getAccessParameters());

        /**
         * At this point the authorization is complete, if the authorization request
         * required a client redirect we do it now
         */
        if (authParams.getClientRedirectUrl() != null) {
            try {
                servletResponse.sendRedirect(authParams.getClientRedirectUrl());
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new ShimException("Error occurred redirecting to :" + authParams.getRedirectUri());
            }
            return null;
        }

        return ok(response);
    }

    /**
     * Sets pass through authentication required by spring.
     */
    private void setPassThroughAuthentication(String username, String shim) {

        SecurityContextHolder.getContext().setAuthentication(new ShimAuthentication(username, shim));
    }
}
