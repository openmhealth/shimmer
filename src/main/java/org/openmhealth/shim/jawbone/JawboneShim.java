package org.openmhealth.shim.jawbone;

import org.openmhealth.shim.AuthorizationRequestParameters;
import org.openmhealth.shim.OAuth2Shim;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Encapsulates parameters specific to jawbone api.
 */
public class JawboneShim implements OAuth2Shim {

    private static final String DATA_URL = "https://jawbone.com/nudge/api/v.1.1/users/@me/";

    private static final String AUTHORIZE_URL = "https://jawbone.com/auth/oauth2/auth";

    private static final String TOKEN_URL = "https://jawbone.com/auth/oauth2/token";

    public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg";

    public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19";

    public static final ArrayList<String> JAWBONE_SCOPES =
        new ArrayList<String>(Arrays.asList("extended_read", "weight_read",
            "cardiac_read", "meal_read", "move_read", "sleep_read"));

    public void trigger(OAuth2RestOperations restTemplate) {
        getData(restTemplate, null);
    }

    public ResponseEntity<String> getData(OAuth2RestOperations restTemplate, Map<String, Object> params) {
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

    @Override
    public AuthorizationRequestParameters getAuthorizationRequestParameters(
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
}
