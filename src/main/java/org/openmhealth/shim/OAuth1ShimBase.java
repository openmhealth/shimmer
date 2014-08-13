package org.openmhealth.shim;

import oauth.signpost.OAuth;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Common code for all OAuth1.0 based shims.
 */
public abstract class OAuth1ShimBase implements Shim, OAuth1Shim {

    protected HttpClient httpClient = HttpClients.createDefault();

    private AuthorizationRequestParametersRepo authorizationRequestParametersRepo;

    protected OAuth1ShimBase(AuthorizationRequestParametersRepo authorizationRequestParametersRepo) {
        this.authorizationRequestParametersRepo = authorizationRequestParametersRepo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthorizationRequestParameters getAuthorizationRequestParameters(
        String username,
        Map<String, String> addlParameters
    ) throws ShimException {

        String stateKey = OAuth1Utils.generateStateKey();

        HttpRequestBase tokenRequest = null;
        try {
            String callbackUrl =
                "http://localhost:8080/authorize/" + getShimKey() + "/callback?state=" + stateKey;

            Map<String, String> requestTokenParameters = new HashMap<>();
            requestTokenParameters.put("oauth_callback", callbackUrl);

            String initiateAuthUrl = getBaseRequestTokenUrl();

            tokenRequest =
                getRequestTokenRequest(initiateAuthUrl, null, null, requestTokenParameters);

            HttpResponse httpResponse = httpClient.execute(tokenRequest);

            Map<String, String> tokenParameters = OAuth1Utils.parseRequestTokenResponse(httpResponse);

            String token = tokenParameters.get(OAuth.OAUTH_TOKEN);
            String tokenSecret = tokenParameters.get(OAuth.OAUTH_TOKEN_SECRET);

            if (tokenSecret == null) {
                throw new ShimException("Request token could not be retrieved");
            }

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
            authorizationRequestParametersRepo.save(parameters);

            return parameters;
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            throw new ShimException("HTTP Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Unable to initiate OAuth1 authorization, " +
                "could not parse token parameters");
        } finally {
            if (tokenRequest != null) {
                tokenRequest.releaseConnection();
            }
        }
    }

    @Override
    public AuthorizationResponse handleAuthorizationResponse(HttpServletRequest servletRequest) throws ShimException {

        // Fetch the access token.
        String stateKey = servletRequest.getParameter("state");
        String requestToken = servletRequest.getParameter(OAuth.OAUTH_TOKEN);
        final String requestVerifier = servletRequest.getParameter(OAuth.OAUTH_VERIFIER);

        AuthorizationRequestParameters authParams =
            authorizationRequestParametersRepo.findByStateKey(stateKey);
        if (authParams == null) {
            throw new ShimException("Invalid state, could not find " +
                "corresponding auth parameters");
        }

        // Get the token secret from the original access request.
        String requestTokenSecret = authParams.getRequestParams().get(OAuth.OAUTH_TOKEN_SECRET);

        HttpResponse response;
        HttpRequestBase accessTokenRequest = null;
        try {
            accessTokenRequest = getAccessTokenRequest(getBaseTokenUrl(),
                requestToken, requestTokenSecret, new HashMap<String, String>() {{
                    put(OAuth.OAUTH_VERIFIER, requestVerifier);
                }});
            response = httpClient.execute(accessTokenRequest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not retrieve response from token URL");
        } finally {
            if (accessTokenRequest != null) {
                accessTokenRequest.releaseConnection();
            }
        }
        Map<String, String> accessTokenParameters = OAuth1Utils.parseRequestTokenResponse(response);
        String accessToken = accessTokenParameters.get(OAuth.OAUTH_TOKEN);
        String accessTokenSecret = accessTokenParameters.get(OAuth.OAUTH_TOKEN_SECRET);

        if (accessToken == null) {
            throw new ShimException("Access token could not be retrieved");
        }

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

    protected HttpRequestBase getSignedRequest(String unsignedUrl,
                                               String token,
                                               String tokenSecret,
                                               Map<String, String> oauthParams) throws ShimException {
        return OAuth1Utils.getSignedRequest(
            unsignedUrl,
            getClientId(),
            getClientSecret(),
            token, tokenSecret, oauthParams);
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

    /**
     * Some external data providers require POST vs GET.
     * In which case the signing of the requests may differ.
     *
     * @param unsignedUrl - The unsigned URL for the request.
     * @param token       - The request token or access token.
     * @param tokenSecret - The token secret, if any.
     * @param oauthParams - Any additional Oauth params.
     * @return - The appropriate request, signed.
     * @throws ShimException
     */
    protected HttpRequestBase getRequestTokenRequest(String unsignedUrl,
                                                     String token,
                                                     String tokenSecret,
                                                     Map<String, String> oauthParams) throws ShimException {
        if (HttpMethod.GET == getRequestTokenMethod()) {
            return new HttpGet(signUrl(unsignedUrl, token, tokenSecret, oauthParams).toString());
        } else {
            return getSignedRequest(unsignedUrl, token, tokenSecret, oauthParams);
        }
    }

    /**
     * NOTE: Same as getRequestTokenRequest with difference being that this is for access tokens.
     * <p/>
     * Some external data providers require POST vs GET.
     * In which case the signing of the requests may differ.
     *
     * @param unsignedUrl - The unsigned URL for the request.
     * @param token       - The request token or access token.
     * @param tokenSecret - The token secret, if any.
     * @param oauthParams - Any additional Oauth params.
     * @return - The appropriate request, signed.
     * @throws ShimException
     */
    protected HttpRequestBase getAccessTokenRequest(String unsignedUrl,
                                                    String token,
                                                    String tokenSecret,
                                                    Map<String, String> oauthParams) throws ShimException {
        if (HttpMethod.GET == getAccessTokenMethod()) {
            return new HttpGet(signUrl(unsignedUrl, token, tokenSecret, oauthParams).toString());
        } else {
            return getSignedRequest(unsignedUrl, token, tokenSecret, oauthParams);
        }
    }

    protected HttpMethod getRequestTokenMethod() {
        return HttpMethod.GET;
    }

    protected HttpMethod getAccessTokenMethod() {
        return HttpMethod.GET;
    }
}
