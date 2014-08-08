package org.openmhealth.shim;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.UrlStringRequestAdapter;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.signature.QueryStringSigningStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for common OAuth1 tasks.
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

        String tokenString = null;
        try {
            tokenString = IOUtils.toString(requestTokenResponse.getEntity().getContent(), "UTF-8");
        } catch (IOException e) {
            throw new ShimException("Error reading request token", e);
        }
        HttpParameters responseParams = OAuth.decodeForm(tokenString);
        Map<String, String> token = new HashMap<String, String>();
        token.put(
            OAuth.OAUTH_TOKEN,
            responseParams.getFirst(OAuth.OAUTH_TOKEN));
        token.put(
            OAuth.OAUTH_TOKEN_SECRET,
            responseParams.getFirst(OAuth.OAUTH_TOKEN_SECRET));
        return token;
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
            HttpParameters httpParameters = new HttpParameters();
            for (String key : oAuthParameters.keySet()) {
                httpParameters.put(key, oAuthParameters.get(key));
            }
            consumer.setAdditionalParameters(httpParameters);
        }

        // Sign the URL.
        URL url = null;
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
