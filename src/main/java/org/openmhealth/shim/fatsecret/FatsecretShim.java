package org.openmhealth.shim.fatsecret;

import com.fasterxml.jackson.databind.ObjectMapper;
import oauth.signpost.OAuth;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.openmhealth.shim.*;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Encapsulates parameters specific to fatsecret api.
 */
public class FatsecretShim implements Shim {

    public static final String SHIM_KEY = "fatsecret";

    private static final String DATA_URL = "http://platform.fatsecret.com/rest/server.api";

    private static final String REQUEST_TOKEN_URL = "http://www.fatsecret.com/oauth/token";

    private static final String AUTHORIZE_URL = "http://www.fatsecret.com/oauth/authorize";

    private static final String TOKEN_URL = "http://www.fatsecret.com/oauth/authorize/";

    public static final String FATSECRET_CLIENT_ID = "d1c59d7f9c8243f0b2eaef9ea43278a0";

    public static final String FATSECRET_CLIENT_SECRET = "c16dd2eeea804a7cba1180293d4b770c";

    private HttpClient httpClient = HttpClients.createDefault();

    private ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String, AuthorizationRequestParameters> AUTH_PARAMS_REPO = new LinkedHashMap<>();

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthorizationRequestParameters getAuthorizationRequestParameters(
        String username,
        Map<String, String> addlParameters
    ) throws ShimException {

        String stateKey = generateStateKey();

        try {
            String callbackUrl =
                URLEncoder.encode("http://localhost:8080/authorize/fatsecret/callback" +
                    "?state=" + stateKey + "&username=" + username, "UTF-8");

            Map<String, String> requestTokenParameters = new HashMap<>();
            requestTokenParameters.put("oauth_callback", callbackUrl);

            String initiateAuthUrl = REQUEST_TOKEN_URL;
            URL signedURL = signUrl(initiateAuthUrl, null, null, requestTokenParameters);

            HttpResponse response = httpClient.execute(new HttpGet(signedURL.toString()));

            Map<String, String> tokenParameters = OAuth1Utils.parseRequestTokenResponse(response);

            String token = tokenParameters.get(OAuth.OAUTH_TOKEN);
            String tokenSecret = tokenParameters.get(OAuth.OAUTH_TOKEN_SECRET);

            URL authorizeUrl = signUrl(AUTHORIZE_URL, token, tokenSecret, null);
            System.out.println("The authorization url is: ");
            System.out.println(authorizeUrl);

            /**
             * Build the auth parameters entity to return
             */
            AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
            parameters.setRedirectUri(callbackUrl);
            parameters.setStateKey(stateKey);
            parameters.setHttpMethod(HttpMethod.GET);
            parameters.setAuthorizationUrl(authorizeUrl.toString());
            parameters.setRequestParams(tokenParameters);

            /**
             * Store the parameters in a repo.
             */
            AUTH_PARAMS_REPO.put(stateKey, parameters);

            return parameters;
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            throw new ShimException("HTTP Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Unable to initiate OAuth1 authorization, " +
                "could not parse token parameters");
        }
    }

    @Override
    public AuthorizationResponse handleAuthorizationResponse(HttpServletRequest servletRequest) throws ShimException {

        // Fetch the access token.
        String username = servletRequest.getParameter("username");
        String stateKey = servletRequest.getParameter("state");
        String requestToken = servletRequest.getParameter(OAuth.OAUTH_TOKEN);
        final String requestVerifier = servletRequest.getParameter(OAuth.OAUTH_VERIFIER);

        AuthorizationRequestParameters authParams = AUTH_PARAMS_REPO.get(stateKey);
        if (authParams == null) {
            throw new ShimException("Invalid state, could not find " +
                "corresponding auth parameters");
        }

        // Get the token secret from the original access request.
        String requestTokenSecret = authParams.getRequestParams().get(OAuth.OAUTH_TOKEN_SECRET);

        URL signedUrl = signUrl(TOKEN_URL,
            requestToken, requestTokenSecret, new HashMap<String, String>() {{
                put(OAuth.OAUTH_VERIFIER, requestVerifier);
            }});

        HttpResponse response = null;
        try {
            response = httpClient.execute(new HttpGet(signedUrl.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Signed URL for access token is: \n\n" + signedUrl);
        Map<String, String> accessTokenParameters = OAuth1Utils.parseRequestTokenResponse(response);
        String accessToken = accessTokenParameters.get(OAuth.OAUTH_TOKEN);
        String accessTokenSecret = accessTokenParameters.get(OAuth.OAUTH_TOKEN_SECRET);

        AccessParameters accessParameters = new AccessParameters();
        accessParameters.setUsername(username);
        accessParameters.setAccessToken(accessToken);
        accessParameters.setTokenSecret(accessTokenSecret);
        accessParameters.setAdditionalParameters(new HashMap<String, Object>() {{
            put(OAuth.OAUTH_VERIFIER, requestVerifier);
        }});

        return AuthorizationResponse.authorized(accessParameters);
    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) {
        return null;
    }

    protected URL signUrl(String unsignedUrl, String token, String tokenSecret, Map<String, String> oauthParams)
        throws ShimException {
        return OAuth1Utils.buildSignedUrl(
            unsignedUrl,
            FATSECRET_CLIENT_ID,
            FATSECRET_CLIENT_SECRET,
            token, tokenSecret, oauthParams);
    }

    /**
     * Return a state key identifier for access requests.
     *
     * @return - random UUID String
     */
    private String generateStateKey() {
        return UUID.randomUUID().toString();
    }
}
