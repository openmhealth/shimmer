/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmhealth.shim;

import oauth.signpost.OAuth;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.openmhealth.shimmer.configuration.DeploymentSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.DESC;


/**
 * @author Danilo Bonilla
 * @author Emerson Farrugia
 */
public abstract class OAuth1Shim implements Shim {

    @Autowired
    private AccessParametersRepo accessParametersRepo;

    @Autowired
    private AuthorizationRequestParametersRepo authorizationRequestParametersRepo;

    @Autowired
    private DeploymentSettings deploymentSettings;

    protected HttpClient httpClient = HttpClients.createDefault();


    protected abstract OAuth1ClientSettings getClientSettings();

    @Override
    public boolean isConfigured() {

        return getClientSettings().hasClientId();
    }

    protected abstract String getRequestTokenUrl();

    @SuppressWarnings("unchecked")
    public AuthorizationRequestParameters getAuthorizationRequestParameters(String username,
            Map<String, String> additionalParameters)
            throws ShimException {

        String stateKey = OAuth1Utils.generateStateKey();
        AccessParameters accessParams = accessParametersRepo
                .findByUsernameAndShimKey(username, getShimKey(), new Sort(DESC, "dateCreated"));

        if (accessParams != null && accessParams.getAccessToken() != null) {
            return AuthorizationRequestParameters.authorized();
        }

        HttpRequestBase tokenRequest = null;

        try {
            String callbackUrl = deploymentSettings.getRedirectUrl(getShimKey(), stateKey);

            Map<String, String> requestTokenParameters = new HashMap<>();
            requestTokenParameters.put("oauth_callback", callbackUrl);

            tokenRequest =
                    getRequestTokenRequest(getRequestTokenUrl(), null, null, requestTokenParameters);

            HttpResponse httpResponse = httpClient.execute(tokenRequest);

            Map<String, String> tokenParameters = OAuth1Utils.parseRequestTokenResponse(httpResponse);

            String token = tokenParameters.get(OAuth.OAUTH_TOKEN);
            String tokenSecret = tokenParameters.get(OAuth.OAUTH_TOKEN_SECRET);

            if (tokenSecret == null) {
                throw new ShimException("Request token could not be retrieved");
            }

            URL authorizeUrl = signUrl(getUserAuthorizationUrl(), token, tokenSecret, null);
            System.out.println("The authorization url is: ");
            System.out.println(authorizeUrl);

            /**
             * Build the auth parameters entity to return
             */
            AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
            parameters.setUsername(username);
            parameters.setRedirectUri(callbackUrl);
            parameters.setStateKey(stateKey);
            parameters.setAuthorizationUrl(authorizeUrl.toString());
            parameters.setRequestParams(tokenParameters);

            return authorizationRequestParametersRepo.save(parameters);
        }
        catch (HttpClientErrorException e) {
            e.printStackTrace();
            throw new ShimException("HTTP Error: " + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Unable to initiate OAuth1 authorization, could not parse token parameters");
        }
        finally {
            if (tokenRequest != null) {
                tokenRequest.releaseConnection();
            }
        }
    }

    @Override
    public AuthorizationResponse processRedirect(HttpServletRequest servletRequest) throws ShimException {

        // Fetch the access token.
        String stateKey = servletRequest.getParameter("state");
        String requestToken = servletRequest.getParameter(OAuth.OAUTH_TOKEN);
        final String requestVerifier = servletRequest.getParameter(OAuth.OAUTH_VERIFIER);

        AuthorizationRequestParameters authParams = authorizationRequestParametersRepo.findByStateKey(stateKey);
        if (authParams == null) {
            throw new ShimException("Invalid state, could not find corresponding auth parameters");
        }

        // Get the token secret from the original access request.
        String requestTokenSecret = authParams.getRequestParams().get(OAuth.OAUTH_TOKEN_SECRET);

        HttpResponse response;
        HttpRequestBase accessTokenRequest = null;
        try {
            accessTokenRequest = getAccessTokenRequest(getAccessTokenUrl(),
                    requestToken, requestTokenSecret, new HashMap<String, String>() {{
                        put(OAuth.OAUTH_VERIFIER, requestVerifier);
                    }});
            response = httpClient.execute(accessTokenRequest);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not retrieve response from token URL");
        }
        finally {
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
        accessParameters.setClientId(getClientSettings().getClientId());
        accessParameters.setClientSecret(getClientSettings().getClientSecret());
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


    protected void loadAdditionalAccessParameters(HttpServletRequest request, AccessParameters accessParameters) {
        //noop, override if additional parameters must be set here
    }

    protected HttpRequestBase getSignedRequest(String unsignedUrl, String token, String tokenSecret,
            Map<String, String> oauthParams)
            throws ShimException {

        return OAuth1Utils
                .getSignedRequest(
                        unsignedUrl,
                        getClientSettings().getClientId(),
                        getClientSettings().getClientSecret(),
                        token,
                        tokenSecret,
                        oauthParams);
    }

    protected URL signUrl(String unsignedUrl, String token, String tokenSecret, Map<String, String> oauthParams)
            throws ShimException {

        return OAuth1Utils
                .buildSignedUrl(
                        unsignedUrl,
                        getClientSettings().getClientId(),
                        getClientSettings().getClientSecret(),
                        token,
                        tokenSecret,
                        oauthParams);
    }

    /**
     * Some external data providers require POST vs GET. In which case the signing of the requests may differ.
     *
     * @param unsignedUrl - The unsigned URL for the request.
     * @param token - The request token or access token.
     * @param tokenSecret - The token secret, if any.
     * @param oauthParams - Any additional Oauth params.
     * @return - The appropriate request, signed.
     */
    protected HttpRequestBase getRequestTokenRequest(String unsignedUrl, String token, String tokenSecret,
            Map<String, String> oauthParams)
            throws ShimException {

        if (HttpMethod.GET == getRequestTokenMethod()) {
            return new HttpGet(signUrl(unsignedUrl, token, tokenSecret, oauthParams).toString());
        }
        else {
            return getSignedRequest(unsignedUrl, token, tokenSecret, oauthParams);
        }
    }

    /**
     * NOTE: Same as getRequestTokenRequest with difference being that this is for access tokens.
     * <p>
     * Some external data providers require POST vs GET. In which case the signing of the requests may differ.
     *
     * @param unsignedUrl - The unsigned URL for the request.
     * @param token - The request token or access token.
     * @param tokenSecret - The token secret, if any.
     * @param oauthParams - Any additional Oauth params.
     * @return - The appropriate request, signed.
     */
    protected HttpRequestBase getAccessTokenRequest(String unsignedUrl, String token, String tokenSecret,
            Map<String, String> oauthParams)
            throws ShimException {

        if (HttpMethod.GET == getAccessTokenMethod()) {
            return new HttpGet(signUrl(unsignedUrl, token, tokenSecret, oauthParams).toString());
        }
        else {
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
