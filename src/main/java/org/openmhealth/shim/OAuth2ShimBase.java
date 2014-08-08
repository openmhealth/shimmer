package org.openmhealth.shim;


import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public abstract class OAuth2ShimBase implements Shim, OAuth2Shim {

    public static LinkedHashMap<String, AccessTokenRequest> ACCESS_REQUEST_REPO = new LinkedHashMap<>();

    protected abstract AuthorizationRequestParameters getAuthorizationRequestParameters(
        final String username, final UserRedirectRequiredException exception);

    protected abstract ResponseEntity<String> getData(
        OAuth2RestOperations restTemplate, Map<String, Object> params);

    @Override
    public AuthorizationRequestParameters getAuthorizationRequestParameters(String username,
                                                                            Map<String, String> addlParameters) {
        OAuth2RestOperations restTemplate = restTemplate();
        try {
            trigger(restTemplate);
            return AuthorizationRequestParameters.authorized();
        } catch (UserRedirectRequiredException e) {
            /**
             * If an exception was thrown it means a redirect is required
             * for user's external authorization with toolmaker.
             */
            AccessTokenRequest accessTokenRequest =
                restTemplate.getOAuth2ClientContext().getAccessTokenRequest();
            String stateKey = accessTokenRequest.getStateKey();
            ACCESS_REQUEST_REPO.put(stateKey, accessTokenRequest);
            return getAuthorizationRequestParameters(username, e);
        }
    }

    @Override
    public AuthorizationResponse handleAuthorizationResponse(HttpServletRequest servletRequest) {
        String state = servletRequest.getParameter("state");
        String code = servletRequest.getParameter("code");
        OAuth2RestOperations restTemplate = restTemplate(state, code);
        try {
            trigger(restTemplate);
            OAuth2AccessToken accessToken = restTemplate.getAccessToken();
            AccessParameters accessParameters = new AccessParameters();
            accessParameters.setAccessToken(accessToken.getValue());
            accessParameters.setStateKey(state);
            return AuthorizationResponse.authorized(accessParameters);
        } catch (OAuth2Exception e) {
            //TODO: OAuth2Exception may include other stuff
            System.out.println("Problem trying out the token!");
            e.printStackTrace();
            return AuthorizationResponse.error(e.getMessage());
        }
    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) {
        return ShimDataResponse.result(getData(restTemplate(), Collections.<String, Object>emptyMap()));
    }

    public void trigger(OAuth2RestOperations restTemplate) {
        getData(restTemplate, null);
    }

    protected OAuth2RestOperations restTemplate(String stateKey, String code) {
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

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(getResource(), context);
        AccessTokenProviderChain tokenProviderChain =
            new AccessTokenProviderChain(new ArrayList<>(
                Arrays.asList(getAuthorizationCodeAccessTokenProvider())));
        tokenProviderChain.setClientTokenServices(new InMemoryTokenRepo());
        restTemplate.setAccessTokenProvider(tokenProviderChain);
        return restTemplate;
    }

    protected OAuth2RestOperations restTemplate() {
        return restTemplate(null, null);
    }
}
