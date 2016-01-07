/*
 * Copyright 2015 Open mHealth
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
 */

package org.openmhealth.shim;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.UrlStringRequestAdapter;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.signature.QueryStringSigningStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utilities for common OAuth1 tasks.
 *
 * @author Danilo Bonilla
 */
public class OAuth1Utils {

    /**
     * Creates an oauth consumer used for signing request URLs.
     *
     * @return The OAuthConsumer.
     */
    public static OAuthConsumer createOAuthConsumer(String clientId, String clientSecret) {
        OAuthConsumer consumer =
            new DefaultOAuthConsumer(clientId, clientSecret);
        consumer.setSigningStrategy(new QueryStringSigningStrategy());
        return consumer;
    }


    /**
     * Parses the request_token response from an initial oauth1 initiate request.
     *
     * @param requestTokenResponse - Response from external data provider
     * @return - Map with token and token secret.
     * @throws ShimException
     */
    public static Map<String, String> parseRequestTokenResponse(
        HttpResponse requestTokenResponse) throws ShimException {

        String tokenString;
        try {
            tokenString = IOUtils.toString(requestTokenResponse.getEntity().getContent(), "UTF-8");
        } catch (IOException e) {
            throw new ShimException("Error reading request token", e);
        }
        HttpParameters responseParams = OAuth.decodeForm(tokenString);
        Map<String, String> token = new HashMap<>();
        token.put(
            OAuth.OAUTH_TOKEN,
            responseParams.getFirst(OAuth.OAUTH_TOKEN));
        token.put(
            OAuth.OAUTH_TOKEN_SECRET,
            responseParams.getFirst(OAuth.OAUTH_TOKEN_SECRET));
        return token;
    }

    /**
     * Return a state key identifier for access requests.
     *
     * @return - random UUID String
     */
    public static String generateStateKey() {
        return UUID.randomUUID().toString();
    }

    /**
     * Signs an HTTP post request for cases where OAuth 1.0 posts are
     * required instead of GET.
     *
     * @param unsignedUrl     - The unsigned URL
     * @param clientId        - The external provider assigned client id
     * @param clientSecret    - The external provider assigned client secret
     * @param token           - The access token
     * @param tokenSecret     - The 'secret' parameter to be used (Note: token secret != client secret)
     * @param oAuthParameters - Any additional parameters
     * @return The request to be signed and sent to external data provider.
     */
    public static HttpRequestBase getSignedRequest(HttpMethod method,
                                                   String unsignedUrl,
                                                   String clientId,
                                                   String clientSecret,
                                                   String token,
                                                   String tokenSecret,
                                                   Map<String, String> oAuthParameters) throws ShimException {

        URL requestUrl = buildSignedUrl(unsignedUrl, clientId, clientSecret, token, tokenSecret, oAuthParameters);
        String[] signedParams = requestUrl.toString().split("\\?")[1].split("&");

        HttpRequestBase postRequest = method == HttpMethod.GET ?
            new HttpGet(unsignedUrl) : new HttpPost(unsignedUrl);
        String oauthHeader = "";
        for (String signedParam : signedParams) {
            String[] parts = signedParam.split("=");
            oauthHeader += parts[0] + "=\"" + parts[1] + "\",";
        }
        oauthHeader = "OAuth " + oauthHeader.substring(0, oauthHeader.length() - 1);
        postRequest.setHeader("Authorization", oauthHeader);
        CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(clientId, clientSecret);
        consumer.setSendEmptyTokens(false);
        if (token != null) {
            consumer.setTokenWithSecret(token, tokenSecret);
        }
        try {
            consumer.sign(postRequest);
            return postRequest;
        } catch (
            OAuthMessageSignerException
                | OAuthExpectationFailedException
                | OAuthCommunicationException e) {
            e.printStackTrace();
            throw new ShimException("Could not sign POST request, cannot continue");
        }
    }

    public static HttpRequestBase getSignedRequest(String unsignedUrl,
                                                   String clientId,
                                                   String clientSecret,
                                                   String token,
                                                   String tokenSecret,
                                                   Map<String, String> oAuthParameters) throws ShimException {
        return getSignedRequest(HttpMethod.POST,
            unsignedUrl, clientId, clientSecret, token, tokenSecret, oAuthParameters);
    }

    /**
     * Builds a signed URL with the given parameters
     *
     * @param unsignedUrl     - The unsigned URL
     * @param clientId        - The external provider assigned client id
     * @param clientSecret    - The external provider assigned client secret
     * @param token           - The access token
     * @param tokenSecret     - The 'secret' parameter to be used (Note: token secret != client secret)
     * @param oAuthParameters - Any additional parameters
     * @return A Signed URL
     * @throws ShimException
     */
    public static URL buildSignedUrl(
        String unsignedUrl,
        String clientId,
        String clientSecret,
        String token,
        String tokenSecret,
        Map<String, String> oAuthParameters) throws ShimException {

        // Build the oauth consumer used for signing requests.
        OAuthConsumer consumer = createOAuthConsumer(clientId, clientSecret);

        if (token != null) {
            consumer.setTokenWithSecret(token, tokenSecret);
        }

        // Add any additional parameters.
        if (oAuthParameters != null) {
            try {
                HttpParameters httpParameters = new HttpParameters();
                for (String key : oAuthParameters.keySet()) {
                    if (key.equals(OAuth.OAUTH_CALLBACK)) {
                        httpParameters.put(key, URLEncoder.encode(oAuthParameters.get(key), "UTF-8"));
                    } else {
                        httpParameters.put(key, oAuthParameters.get(key));
                    }
                }
                consumer.setAdditionalParameters(httpParameters);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new ShimException("Could not URL Encode callbackUrl, cannot continue");
            }
        }

        // Sign the URL.
        URL url;
        try {
            UrlStringRequestAdapter adapter = new UrlStringRequestAdapter(unsignedUrl);
            consumer.sign(adapter);
            url = new URL(adapter.getRequestUrl());
        } catch (MalformedURLException
            | OAuthExpectationFailedException
            | OAuthCommunicationException
            | OAuthMessageSignerException
            e) {
            throw new ShimException("Error signing URL", e);
        }

        return url;
    }
}
