/*
 * Copyright 2015 Open mHealth
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
 */

package org.openmhealth.shimmer.common.controller;

import org.openmhealth.shim.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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

    private static final String AUTH_SUCCESS_URL = "/#authorizationComplete/success";
    private static final String AUTH_FAILURE_URL = "/#authorizationComplete/failure";
    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    private AuthorizationRequestParametersRepo authParametersRepo;

    @Autowired
    private ShimRegistry shimRegistry;

    @Autowired
    private ShimServerConfig shimServerProperties;


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
     * @param clientRedirectUrl The URL to which the external shim client  will be redirected after authorization is
     * complete.
     * @param shim The shim registry key of the shim we're approving
     * @return AuthorizationRequest parameters, including a boolean flag if already authorized.
     */
    @RequestMapping(value = "/authorize/{shim}", produces = APPLICATION_JSON_VALUE)
    public AuthorizationRequestParameters authorize(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "client_redirect_url", required = false) String clientRedirectUrl,
            @PathVariable("shim") String shim) throws ShimException {

        setPassThroughAuthentication(username, shim);
        AuthorizationRequestParameters authParams = shimRegistry.getShim(shim)
                .getAuthorizationRequestParameters(username, Collections.emptyMap());
        /**
         * Save authorization parameters to local repo. They will be
         * re-fetched via stateKey upon approval.
         */
        authParams.setUsername(username);
        authParams.setClientRedirectUrl(clientRedirectUrl);

        authParametersRepo.save(authParams);

        return authParams;
    }

    /**
     * Endpoint for removing authorizations for a given user and shim.
     *
     * @param username The user record for which we're removing shim access.
     * @param shim The shim registry key of the shim authorization we're removing.
     * @return Simple response message.
     */
    @RequestMapping(value = "/de-authorize/{shim}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public List<String> removeAuthorization(
            @RequestParam(value = "username") String username,
            @PathVariable("shim") String shim)
            throws ShimException {

        List<AccessParameters> accessParameters = accessParametersRepo.findAllByUsernameAndShimKey(username, shim);

        accessParameters.forEach(accessParametersRepo::delete);

        return singletonList("Success: Authorization Removed.");
    }

    /**
     * Endpoint for handling approvals from external data providers
     *
     * @param servletRequest Request posted by the external data provider.
     * @return AuthorizationResponse object with details and result: authorize, error, or denied.
     */
    @RequestMapping(value = "/authorize/{shim}/callback", method = {POST, GET}, produces = APPLICATION_JSON_VALUE)
    public AuthorizationResponse approve(
            @PathVariable("shim") String shim,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse)
            throws ShimException {

        String stateKey = servletRequest.getParameter("state");
        AuthorizationRequestParameters authParams = authParametersRepo.findByStateKey(stateKey);
        if (authParams == null) {
            throw new ShimException("Invalid state key, original access request not found. Cannot authorize.");
        }
        else {
            setPassThroughAuthentication(authParams.getUsername(), shim);
            AuthorizationResponse response = shimRegistry.getShim(shim).handleAuthorizationResponse(servletRequest);
            /**
             * Save the access parameters to local repo.
             * They will be re-fetched via username and path parameters
             * for future requests.
             */
            response.getAccessParameters().setUsername(authParams.getUsername());
            response.getAccessParameters().setShimKey(shim);
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

            String authorizationStatusURL = AUTH_FAILURE_URL;
            if (response.getType().equals(AuthorizationResponse.Type.AUTHORIZED)) {

                authorizationStatusURL = AUTH_SUCCESS_URL;
            }

            try{
                servletResponse.sendRedirect(shimServerProperties.getCallbackUrlBase() + authorizationStatusURL);
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new ShimException("Error occurred in redirecting to completion URL");
            }

            return response;
        }
    }

    /**
     * Sets pass through authentication required by spring.
     */
    private void setPassThroughAuthentication(String username, String shim) {
        SecurityContextHolder.getContext().setAuthentication(new ShimAuthentication(username, shim));
    }
}
