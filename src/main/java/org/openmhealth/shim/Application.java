package org.openmhealth.shim;

import org.openmhealth.shim.jawbone.JawboneShim;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@RestController
public class Application extends WebSecurityConfigurerAdapter {

    private JawboneShim jawboneShim = new JawboneShim();

    public static LinkedHashMap<String, AccessTokenRequest> ACCESS_REQUEST_REPO =
        new LinkedHashMap<String, AccessTokenRequest>();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
    }

    /**
     * URL for triggering domain approval.
     *
     * @param domain - The toolmaker domain to approve
     * @return - 'Authorized' message or AuthorizationRequest parameters
     */
    @RequestMapping("/authorize/{domain}")
    public ResponseEntity<Object> authorize(@PathVariable("domain") String domain) {
        OAuth2RestOperations restTemplate = restTemplate();
        try {
            jawboneShim.trigger(restTemplate);
            return new ResponseEntity<Object>("Authorized", HttpStatus.OK);
        } catch (UserRedirectRequiredException e) {
            /**
             * If an exception was thrown it means a redirect is required
             * for user's external authorization with toolmaker.
             */
            AccessTokenRequest accessTokenRequest =
                restTemplate.getOAuth2ClientContext().getAccessTokenRequest();
            String stateKey = accessTokenRequest.getStateKey();
            ACCESS_REQUEST_REPO.put(stateKey, accessTokenRequest);
            return new ResponseEntity<Object>(
                jawboneShim.getAuthorizationRequestParameters(e), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/authorize/{domain}/callback",
        method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> approve(@RequestParam("state") String state,
                                          @RequestParam("code") String code) {
        OAuth2RestOperations restTemplate = restTemplate(state, code);
        try {
            jawboneShim.trigger(restTemplate);
        } catch (OAuth2Exception e) {
            System.out.println("Problem trying out the token!");
            e.printStackTrace();
        }
        return new ResponseEntity<String>("Authorized", HttpStatus.OK);
    }

    @RequestMapping("/jawbone")
    public ResponseEntity<String> home() {
        return jawboneShim.getData(restTemplate(), Collections.<String, Object>emptyMap());
    }

    private OAuth2RestOperations restTemplate(String stateKey, String code) {
        DefaultAccessTokenRequest existingRequest = stateKey != null
            && ACCESS_REQUEST_REPO.containsKey(stateKey) ?
            (DefaultAccessTokenRequest) ACCESS_REQUEST_REPO.get(stateKey) : null;

        if (existingRequest != null && code != null) {
            existingRequest.set("code", code);
        }

        DefaultOAuth2ClientContext context =
            new DefaultOAuth2ClientContext(existingRequest != null ?
                existingRequest : new DefaultAccessTokenRequest());
        if (existingRequest != null) {
            context.setPreservedState(stateKey, "NONE");
        }

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(jawboneShim.getResource(), context);
        AccessTokenProviderChain tokenProviderChain =
            new AccessTokenProviderChain(new ArrayList<AuthorizationCodeAccessTokenProvider>(
                Arrays.asList(jawboneShim.getAuthorizationCodeAccessTokenProvider())));

        tokenProviderChain.setClientTokenServices(new InMemoryTokenRepo());
        restTemplate.setAccessTokenProvider(tokenProviderChain);
        return restTemplate;
    }

    private OAuth2RestOperations restTemplate() {
        return restTemplate(null, null);
    }
}
