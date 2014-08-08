package org.openmhealth.shim;

import org.openmhealth.shim.jawbone.JawboneShim;
import org.openmhealth.shim.runkeeper.RunkeeperShim;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;

@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@RestController
public class Application extends WebSecurityConfigurerAdapter {

    private LinkedHashMap<String, Shim> shimRegistry = new LinkedHashMap<String, Shim>() {{
        put(JawboneShim.SHIM_KEY, new JawboneShim());
        put(RunkeeperShim.SHIM_KEY, new RunkeeperShim());
    }};

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
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
    AuthorizationRequestParameters authorize(@PathVariable("shim") String shim) {
        return shimRegistry.get(shim).getAuthorizationRequestParameters(
            Collections.<String, String>emptyMap());
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
                                  HttpServletRequest servletRequest) {
        return shimRegistry.get(shim).handleAuthorizationResponse(servletRequest);
    }

    /**
     * Endpoint for retrieving data from shims.
     *
     * @return - The shim data response wrapper with data from the shim.
     */
    @RequestMapping(value = "/data/{shim}/{dataType}", produces = "application/json")
    public
    @ResponseBody
    ShimDataResponse data(@PathVariable("shim") String shim,
                          HttpServletRequest servletRequest) {
        ShimDataRequest shimDataRequest =
            ShimDataRequest.fromHttpRequest(servletRequest);
        return shimRegistry.get(shim).getData(shimDataRequest);
    }
}
