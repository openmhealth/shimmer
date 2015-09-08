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

package org.openmhealth.shim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.openmhealth.schema.configuration.JacksonConfiguration.newObjectMapper;
import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * @author Danilo Bonilla
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.openmhealth")
@EnableWebSecurity
@RestController
public class Application extends WebSecurityConfigurerAdapter {

    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    private AuthorizationRequestParametersRepo authParametersRepo;

    @Autowired
    private ApplicationAccessParametersRepo applicationAccessParametersRepo;

    @Autowired
    private ShimRegistry shimRegistry;

    // TODO clarify what this is for
    private static final String REDIRECT_OOB = "oob";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /**
         * Allow full anonymous authentication.
         */
        http.csrf().disable()
                .authorizeRequests().anyRequest().permitAll();
    }

    /**
     * Return shims available in the registry and all endpoints.
     *
     * @return list of shims + endpoints in a map.
     */
    @RequestMapping("registry")
    public List<Map<String, Object>> shimList(@RequestParam(value = "available", defaultValue = "") String available)
            throws ShimException {

        List<Map<String, Object>> results = new ArrayList<>();
        List<Shim> shims = "".equals(available) ? shimRegistry.getShims() : shimRegistry.getAvailableShims();

        for (Shim shim : shims) {
            List<String> endpoints = new ArrayList<>();
            for (ShimDataType dataType : shim.getShimDataTypes()) {
                endpoints.add(dataType.name());
            }
            Map<String, Object> row = new HashMap<>();
            row.put("shimKey", shim.getShimKey());
            row.put("label", shim.getLabel());
            row.put("endpoints", endpoints);
            ApplicationAccessParameters parameters = shim.findApplicationAccessParameters();
            if (parameters.getClientId() != null) {
                row.put("clientId", parameters.getClientId());
            }
            if (parameters.getClientSecret() != null) {
                row.put("clientSecret", parameters.getClientSecret());
            }
            results.add(row);
        }
        return results;
    }

    /**
     * Update shim configuration
     *
     * @return list of shims + endpoints in a map.
     */
    @RequestMapping(value = "shim/{shim}/config", method = {GET, PUT, POST})
    public List<String> updateShimConfig(
            @PathVariable("shim") String shimKey,
            @RequestParam("clientId") String clientId,
            @RequestParam("clientSecret") String clientSecret)
            throws ShimException {

        ApplicationAccessParameters parameters = applicationAccessParametersRepo.findByShimKey(shimKey);

        if (parameters == null) {
            parameters = new ApplicationAccessParameters();
            parameters.setShimKey(shimKey);
        }
        parameters.setClientId(clientId);
        parameters.setClientSecret(clientSecret);
        applicationAccessParametersRepo.save(parameters);
        shimRegistry.init();

        return singletonList("success");
    }

    /**
     * Retrieve access parameters for the given username/fragment.
     *
     * @param username username fragment to search.
     * @return List of access parameters.
     */
    @RequestMapping("authorizations")
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
    @RequestMapping("/authorize/{shim}")
    public AuthorizationRequestParameters authorize(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "client_redirect_url", defaultValue = REDIRECT_OOB) String clientRedirectUrl,
            @PathVariable("shim") String shim) throws ShimException {

        setPassThroughAuthentication(username, shim);
        AuthorizationRequestParameters authParams = shimRegistry.getShim(shim)
                .getAuthorizationRequestParameters(username, Collections.<String, String>emptyMap());
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
    @RequestMapping(value = "/de-authorize/{shim}", method = DELETE)
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
    @RequestMapping(value = "/authorize/{shim}/callback", method = {POST, GET})
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
             * required a client redirect we do it now, else just return
             * the authorization response.
             */
            if (authParams.getClientRedirectUrl() != null &&
                    !REDIRECT_OOB.equals(authParams.getClientRedirectUrl())) {
                try {
                    servletResponse.sendRedirect(authParams.getClientRedirectUrl());
                }
                catch (IOException e) {
                    e.printStackTrace();
                    throw new ShimException("Error occurred redirecting to :" + authParams.getRedirectUri());
                }
                return null;
            }
            return response;
        }
    }

    /**
     * Endpoint for retrieving data from shims.
     *
     * @param username User ID record for which to retrieve data, if not approved this will throw ShimException.
     * todo: finish javadoc!
     * @return The shim data response wrapper with data from the shim.
     */
    @RequestMapping(value = "/data/{shim}/{dataType}")
    public ShimDataResponse data(
            @RequestParam(value = "username") String username,
            @PathVariable("shim") String shim,
            @PathVariable("dataType") String dataTypeKey,
            @RequestParam(value = "normalize", defaultValue = "") String normalize,
            @RequestParam(value = "dateStart", defaultValue = "") String dateStart,
            @RequestParam(value = "dateEnd", defaultValue = "") String dateEnd,
            @RequestParam(value = "numToReturn", defaultValue = "50") Long numToReturn)
            throws ShimException {

        setPassThroughAuthentication(username, shim);

        ShimDataRequest shimDataRequest = new ShimDataRequest();

        shimDataRequest.setDataTypeKey(dataTypeKey);

        if(!normalize.equals("")){
            shimDataRequest.setNormalize(Boolean.parseBoolean(normalize));
        }

        if (!"".equals(dateStart)) {
            shimDataRequest.setStartDateTime(LocalDate.parse(dateStart).atStartOfDay().atOffset(UTC));
        }
        if (!"".equals(dateEnd)) {
            shimDataRequest.setEndDateTime(LocalDate.parse(dateEnd).atStartOfDay().atOffset(UTC));
        }
        shimDataRequest.setNumToReturn(numToReturn);

        AccessParameters accessParameters = accessParametersRepo.findByUsernameAndShimKey(
                username, shim, new Sort(Sort.Direction.DESC, "dateCreated"));

        if (accessParameters == null) {
            throw new ShimException("User '" + username + "' has not authorized shim: '" + shim + "'");
        }
        shimDataRequest.setAccessParameters(accessParameters);
        return shimRegistry.getShim(shim).getData(shimDataRequest);
    }

    /**
     * Sets pass through authentication required by spring.
     */
    private void setPassThroughAuthentication(String username, String shim) {
        SecurityContextHolder.getContext().setAuthentication(new ShimAuthentication(username, shim));
    }

    @Bean
    public ObjectMapper objectMapper() {
        return newObjectMapper();
    }
}
