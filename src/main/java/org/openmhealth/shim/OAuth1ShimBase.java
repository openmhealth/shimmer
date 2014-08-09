package org.openmhealth.shim;

import oauth.signpost.OAuth;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Common code for all OAuth1.0 based shims.
 */
public abstract class OAuth1ShimBase implements Shim, OAuth1Shim {

    protected HttpClient httpClient = HttpClients.createDefault();

    private static Map<String, AuthorizationRequestParameters> AUTH_PARAMS_REPO = new LinkedHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public AuthorizationRequestParameters getAuthorizationRequestParameters(
        String username,
        Map<String, String> addlParameters
    ) throws ShimException {

        String stateKey = OAuth1Utils.generateStateKey();

        try {
            String callbackUrl =
                URLEncoder.encode("http://localhost:8080/authorize/" + getShimKey() + "/callback" +
                    "?state=" + stateKey, "UTF-8");

            Map<String, String> requestTokenParameters = new HashMap<>();
            requestTokenParameters.put("oauth_callback", callbackUrl);

            String initiateAuthUrl = getBaseRequestTokenUrl();
            URL signedURL = signUrl(initiateAuthUrl, null, null, requestTokenParameters);

            HttpResponse response = httpClient.execute(new HttpGet(signedURL.toString()));

            Map<String, String> tokenParameters = OAuth1Utils.parseRequestTokenResponse(response);

            String token = tokenParameters.get(OAuth.OAUTH_TOKEN);
            String tokenSecret = tokenParameters.get(OAuth.OAUTH_TOKEN_SECRET);

            URL authorizeUrl = signUrl(getBaseAuthorizeUrl(), token, tokenSecret, null);
            System.out.println("The authorization url is: ");
            System.out.println(authorizeUrl);

            /**
             * Build the auth parameters entity to return
             */
            AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
            parameters.setUsername(username);
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

        URL signedUrl = signUrl(getBaseTokenUrl(),
            requestToken, requestTokenSecret, new HashMap<String, String>() {{
                put(OAuth.OAUTH_VERIFIER, requestVerifier);
            }});

        HttpResponse response;
        try {
            response = httpClient.execute(new HttpGet(signedUrl.toString()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not retrieve response from token URL");
        }

        System.out.println("Signed URL for access token is: \n\n" + signedUrl);
        Map<String, String> accessTokenParameters = OAuth1Utils.parseRequestTokenResponse(response);
        String accessToken = accessTokenParameters.get(OAuth.OAUTH_TOKEN);
        String accessTokenSecret = accessTokenParameters.get(OAuth.OAUTH_TOKEN_SECRET);

        AccessParameters accessParameters = new AccessParameters();
        accessParameters.setClientId(getClientId());
        accessParameters.setClientSecret(getClientSecret());
        accessParameters.setStateKey(stateKey);
        accessParameters.setUsername(authParams.getUsername());
        accessParameters.setAccessToken(accessToken);
        accessParameters.setTokenSecret(accessTokenSecret);
        accessParameters.setAdditionalParameters(new HashMap<String, Object>() {{
            put(OAuth.OAUTH_VERIFIER, requestVerifier);
        }});
        loadAdditionalAccessParameters(servletRequest, accessParameters);
        return AuthorizationResponse.authorized(accessParameters);
    }

    protected void loadAdditionalAccessParameters(
        HttpServletRequest request,
        AccessParameters accessParameters
    ) {
        //noop, override if additional parameters must be set here
    }

    protected URL signUrl(String unsignedUrl,
                          String token,
                          String tokenSecret,
                          Map<String, String> oauthParams)
        throws ShimException {
        return OAuth1Utils.buildSignedUrl(
            unsignedUrl,
            getClientId(),
            getClientSecret(),
            token, tokenSecret, oauthParams);
    }
}
