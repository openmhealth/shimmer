package org.openmhealth.shim.jawbone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmhealth.shim.*;
import org.springframework.http.*;
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
import java.io.IOException;
import java.util.*;

/**
 * Encapsulates parameters specific to jawbone api.
 */
public class JawboneShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "jawbone";

    private static final String DATA_URL = "https://jawbone.com/nudge/api/v.1.1/users/@me/";

    private static final String AUTHORIZE_URL = "https://jawbone.com/auth/oauth2/auth";

    private static final String TOKEN_URL = "https://jawbone.com/auth/oauth2/token";

    public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg";

    public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19";

    public static final ArrayList<String> JAWBONE_SCOPES =
        new ArrayList<String>(Arrays.asList("extended_read", "weight_read",
            "cardiac_read", "meal_read", "move_read", "sleep_read"));

    public JawboneShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                       AccessParametersRepo accessParametersRepo,
                       ShimServerConfig shimServerConfig1) {
        super(authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig1);
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getClientSecret() {
        return JAWBONE_CLIENT_SECRET;
    }

    @Override
    public String getClientId() {
        return JAWBONE_CLIENT_ID;
    }

    @Override
    public String getBaseAuthorizeUrl() {
        return AUTHORIZE_URL;
    }

    @Override
    public String getBaseTokenUrl() {
        return TOKEN_URL;
    }

    @Override
    public List<String> getScopes() {
        return JAWBONE_SCOPES;
    }

    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new JawboneAuthorizationCodeAccessTokenProvider();
    }

    @Override
    public ShimDataRequest getTriggerDataRequest() {
        ShimDataRequest shimDataRequest = new ShimDataRequest();
        shimDataRequest.setDataTypeKey("body_events");
        shimDataRequest.setNumToReturn(1l);
        return shimDataRequest;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return null;
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {
        String urlRequest = DATA_URL;
        urlRequest += "body_events?";

        long numToReturn = 3;

        long startTimeTs = 1405694496;
        long endTimeTs = 1405694498;

        urlRequest += "&start_time=" + startTimeTs;
        urlRequest += "&end_time=" + endTimeTs;
        urlRequest += "&limit=" + numToReturn;

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(urlRequest, byte[].class);
        JsonNode json = null;
        try {
            json = objectMapper.readTree(responseEntity.getBody());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not read response data.");
        }
        return new ResponseEntity<>(ShimDataResponse.result(json), HttpStatus.OK);
    }

    protected AuthorizationRequestParameters getAuthorizationRequestParameters(
        final String username,
        final UserRedirectRequiredException exception) {
        final OAuth2ProtectedResourceDetails resource = getResource();
        String authorizationUrl = exception.getRedirectUri()
            + "?state="
            + exception.getStateKey()
            + "&client_id="
            + resource.getClientId()
            + "&response_type=code"
            + "&scope=" + StringUtils.collectionToDelimitedString(resource.getScope(), " ")
            + "&redirect_uri=" + getCallbackUrl();
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
}
