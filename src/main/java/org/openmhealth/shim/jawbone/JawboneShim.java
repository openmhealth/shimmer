package org.openmhealth.shim.jawbone;

import org.joda.time.DateTime;
import org.openmhealth.shim.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Encapsulates parameters specific to jawbone api.
 */
public class JawboneShim implements Shim, OAuth2Shim {

    private static final String SHIM_KEY = "jawbone";

    private static final String DATA_URL = "https://jawbone.com/nudge/api/v.1.1/users/@me/";

    private static final String AUTHORIZE_URL = "https://jawbone.com/auth/oauth2/auth";

    private static final String TOKEN_URL = "https://jawbone.com/auth/oauth2/token";

    public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg";

    public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19";

    public static final ArrayList<String> JAWBONE_SCOPES =
        new ArrayList<String>(Arrays.asList("extended_read", "weight_read",
            "cardiac_read", "meal_read", "move_read", "sleep_read"));

    public static LinkedHashMap<String, AccessTokenRequest> ACCESS_REQUEST_REPO =
        new LinkedHashMap<String, AccessTokenRequest>();

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public AuthorizationRequestParameters getAuthorizationRequestParameters(Map<String, String> addlParameters) {
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
            return getAuthorizationRequestParameters(e);
        }
    }

    @Override
    public AuthorizationResponse handleAuthorizationResponse(HttpServletRequest servletRequest) {
        String state = servletRequest.getParameter("state");
        String code = servletRequest.getParameter("code");
        OAuth2RestOperations restTemplate = restTemplate(state, code);
        try {
            trigger(restTemplate);
            return AuthorizationResponse.authorized();
        } catch (OAuth2Exception e) {
            //TODO: OAuth2Exception may include other stuff
            System.out.println("Problem trying out the token!");
            e.printStackTrace();
            return AuthorizationResponse.error(e.getMessage());
        }
    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) {
        return ShimDataResponse.result(
            getData(restTemplate(), Collections.<String, Object>emptyMap()));
    }

    public void trigger(OAuth2RestOperations restTemplate) {
        getData(restTemplate, null);
    }

    public OAuth2ProtectedResourceDetails getResource() {
        AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();
        resource.setAccessTokenUri(TOKEN_URL);
        resource.setUserAuthorizationUri(AUTHORIZE_URL);
        resource.setClientId(JAWBONE_CLIENT_ID);
        resource.setClientSecret(JAWBONE_CLIENT_SECRET);
        resource.setScope(JAWBONE_SCOPES);
        resource.setTokenName("access_token");
        resource.setGrantType("authorization_code");
        resource.setUseCurrentUri(true);
        return resource;
    }

    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new JawboneAuthorizationCodeAccessTokenProvider();
    }

    private ResponseEntity<String> getData(OAuth2RestOperations restTemplate, Map<String, Object> params) {
        String urlRequest = DATA_URL;
        urlRequest += "body_events?";

        long numToReturn = 3;

        long startTimeTs = 1405694496;
        long endTimeTs = 1405694498;

        urlRequest += "&start_time=" + startTimeTs;
        urlRequest += "&end_time=" + endTimeTs;
        urlRequest += "&limit=" + numToReturn;
        return restTemplate.getForEntity(urlRequest, String.class);
    }

    private AuthorizationRequestParameters getAuthorizationRequestParameters(
        final UserRedirectRequiredException exception) {
        final OAuth2ProtectedResourceDetails resource = getResource();
        String authorizationUrl = exception.getRedirectUri()
            + "?state="
            + exception.getStateKey()
            + "&client_id="
            + resource.getClientId()
            + "&response_type=code"
            + "&scope=" + StringUtils.collectionToDelimitedString(resource.getScope(), " ")
            + "&redirect_uri=http://localhost:8080/authorize/jawbone/callback";//TODO: Move this to outside
        AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
        parameters.setRedirectUri(exception.getRedirectUri());
        parameters.setStateKey(exception.getStateKey());
        parameters.setHttpMethod(HttpMethod.GET);
        parameters.setAuthorizationUrl(authorizationUrl);
        return parameters;
    }


    /**
     * Simple overrides to base spring class from oauth.
     */
    public class JawboneAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
        public JawboneAuthorizationCodeAccessTokenProvider() {
            this.setTokenRequestEnhancer(new JawboneTokenRequestEnhancer());
        }

        @Override
        protected HttpMethod getHttpMethod() {
            return HttpMethod.GET;
        }
    }

    /**
     * Adds jawbone required parameters to authorization token requests.
     */
    private class JawboneTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("grant_type", resource.getGrantType());
        }
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

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(getResource(), context);
        AccessTokenProviderChain tokenProviderChain =
            new AccessTokenProviderChain(new ArrayList<AuthorizationCodeAccessTokenProvider>(
                Arrays.asList(getAuthorizationCodeAccessTokenProvider())));

        tokenProviderChain.setClientTokenServices(new InMemoryTokenRepo());
        restTemplate.setAccessTokenProvider(tokenProviderChain);
        return restTemplate;
    }

    private OAuth2RestOperations restTemplate() {
        return restTemplate(null, null);
    }
}
