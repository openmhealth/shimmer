package org.openmhealth.shim;

import org.openmhealth.shim.fatsecret.FatsecretShim;
import org.openmhealth.shim.jawbone.JawboneShim;
import org.openmhealth.shim.runkeeper.RunkeeperShim;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;

@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@RestController
public class Application extends WebSecurityConfigurerAdapter {

    private LinkedHashMap<String, Shim> SHIM_REGISTRY = new LinkedHashMap<String, Shim>() {{
        put(JawboneShim.SHIM_KEY, new JawboneShim());
        put(RunkeeperShim.SHIM_KEY, new RunkeeperShim());
        put(FatsecretShim.SHIM_KEY, new FatsecretShim());
    }};

    private LinkedHashMap<String, AccessParameters> ACCESS_PARAM_REPO = new LinkedHashMap<>();
    private LinkedHashMap<String, AuthorizationRequestParameters> AUTH_PARAM_REPO = new LinkedHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /**
         * Allow full anonymous authentication.
         */
        http.authorizeRequests().anyRequest().permitAll();
    }

    /**
     * Endpoint for triggering domain approval.
     *
     * @param shim - The shim registry key of the shim we're approving
     * @return - AuthorizationRequest parameters, including a boolean
     * flag if already authorized.
     */
    @RequestMapping("/authorize/{shim}")
    public
    @ResponseBody
    AuthorizationRequestParameters authorize(@RequestParam(value = "username") String username,
                                             @PathVariable("shim") String shim) throws ShimException {
        setPassThroughAuthentication(username, shim);
        AuthorizationRequestParameters authParams =
            SHIM_REGISTRY.get(shim).getAuthorizationRequestParameters(
                username, Collections.<String, String>emptyMap());
        /**
         * Save authorization parameters to local repo. They will be
         * re-fetched via stateKey upon approval.
         */
        authParams.setUsername(username);
        AUTH_PARAM_REPO.put(authParams.getStateKey(), authParams);
        return authParams;
    }

    /**
     * Endpoint for handling approvals from external data providers
     *
     * @param servletRequest - Request posted by the external data provider.
     * @return - AuthorizationResponse object with details
     * and result: authorize, error, or denied.
     */
    @RequestMapping(value = "/authorize/{shim}/callback",
        method = {RequestMethod.POST, RequestMethod.GET})
    public
    @ResponseBody
    AuthorizationResponse approve(@PathVariable("shim") String shim,
                                  HttpServletRequest servletRequest) throws ShimException {
        String stateKey = servletRequest.getParameter("state");
        if (!AUTH_PARAM_REPO.containsKey(stateKey)) {
            throw new ShimException("Invalid state key, original access " +
                "request not found. Cannot authorize.");
        } else {
            AuthorizationRequestParameters authParams = AUTH_PARAM_REPO.get(stateKey);
            setPassThroughAuthentication(authParams.getUsername(), shim);
            AuthorizationResponse response =
                SHIM_REGISTRY.get(shim).handleAuthorizationResponse(servletRequest);
            /**
             * Save the access parameters to local repo.
             * They will be re-fetched via username and path parameters
             * for future requests.
             */
            response.getAccessParameters().setUsername(authParams.getUsername());
            String accessParamKey = response.getAccessParameters().getUsername() + ":" + shim;
            ACCESS_PARAM_REPO.put(accessParamKey, response.getAccessParameters());
            return response;
        }
    }

    /**
     * Endpoint for retrieving data from shims.
     *
     * @return - The shim data response wrapper with data from the shim.
     */
    @RequestMapping(value = "/data/{shim}/{dataType}")
    public
    @ResponseBody
    ShimDataResponse data(@RequestParam(value = "username") String username,
                          @PathVariable("shim") String shim,
                          HttpServletRequest servletRequest) throws ShimException {
        setPassThroughAuthentication(username, shim);
        ShimDataRequest shimDataRequest =
            ShimDataRequest.fromHttpRequest(servletRequest);
        AccessParameters accessParameters = ACCESS_PARAM_REPO.get(username + ":" + shim);
        if (accessParameters == null) {
            throw new ShimException("User '"
                + username + "' has not authorized shim: '" + shim + "'");
        }
        shimDataRequest.setAccessParameters(accessParameters);
        return SHIM_REGISTRY.get(shim).getData(shimDataRequest);
    }

    /**
     * Sets pass through authentication required by spring.
     */
    private void setPassThroughAuthentication(String username, String shim) {
        SecurityContextHolder.getContext()
            .setAuthentication(new ShimAuthentication(username, shim));
    }
}
