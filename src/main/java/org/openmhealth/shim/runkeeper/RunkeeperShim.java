package org.openmhealth.shim.runkeeper;

import org.joda.time.DateTime;
import org.openmhealth.shim.AuthorizationRequestParameters;
import org.openmhealth.shim.OAuth2ShimBase;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Encapsulates parameters specific to jawbone api.
 */
public class RunkeeperShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "runkeeper";

    private static final String DATA_URL = "https://api.runkeeper.com";

    private static final String AUTHORIZE_URL = "https://runkeeper.com/apps/authorize";

    private static final String TOKEN_URL = "https://runkeeper.com/apps/token";

    public static final String RUNKEEPER_CLIENT_ID = "e83ac7fc3c9c4a89bac029921253d495";

    public static final String RUNKEEPER_CLIENT_SECRET = "fe9e8bd9f60e4ff5812a1b0b4744d5e9";

    public static final ArrayList<String> RUNKEEPER_SCOPES =
        new ArrayList<String>(Arrays.asList(
            "application/vnd.com.runkeeper.FitnessActivityFeed+json"
        ));

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    public OAuth2ProtectedResourceDetails getResource() {
        AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();
        resource.setAccessTokenUri(TOKEN_URL);
        resource.setUserAuthorizationUri(AUTHORIZE_URL);
        resource.setClientId(RUNKEEPER_CLIENT_ID);
        resource.setClientSecret(RUNKEEPER_CLIENT_SECRET);
        resource.setTokenName("access_token");
        resource.setGrantType("authorization_code");
        resource.setUseCurrentUri(true);
        return resource;
    }

    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new RunkeeperTokenRequestEnhancer());
        return provider;
    }

    protected ResponseEntity<String> getData(OAuth2RestOperations restTemplate, Map<String, Object> params) {
        String urlRequest = DATA_URL;
        urlRequest += "/fitnessActivities?";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", RUNKEEPER_SCOPES.get(0));

        long numToSkip = 0;
        long numToReturn = 3;

        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.AUGUST, 1);
        Date endDate = new Date(cal.getTimeInMillis());
        cal.add(Calendar.DATE, -1);
        Date startDate = new Date(cal.getTimeInMillis());

        DateTime startTime = new DateTime(startDate.getTime());
        DateTime endTime = new DateTime(endDate.getTime());

        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        DateTime altEndDate = new DateTime(startDate).plusMillis(1);
        String urlParams = "";

        urlParams +=
            "&noEarlierThan=" + fmt.format(startTime.toDate());
        //urlParams += "&noLaterThan="
        //    + (endTime != null ? fmt.format(endTime.toDate()) : fmt.format(altEndDate.toDate()));
        urlParams += "&noLaterThan=" + fmt.format(endTime.toDate());
        urlParams += "&pageSize=" + numToReturn;

        urlRequest += "".equals(urlParams) ?
            "" : ("?" + urlParams.substring(1, urlParams.length()));

        ResponseEntity<byte[]> response = restTemplate.exchange(
            urlRequest,
            HttpMethod.GET,
            new HttpEntity<byte[]>(headers),
            byte[].class);

        String jsonString = new String(response.getBody());
        return new ResponseEntity<String>(jsonString, HttpStatus.OK);
    }

    protected AuthorizationRequestParameters getAuthorizationRequestParameters(
        final UserRedirectRequiredException exception) {
        final OAuth2ProtectedResourceDetails resource = getResource();
        String authorizationUrl = exception.getRedirectUri()
            + "?state="
            + exception.getStateKey()
            + "&client_id="
            + resource.getClientId()
            + "&response_type=code"
            + "&redirect_uri=http://localhost:8080/authorize/" + getShimKey() + "/callback";//TODO: Move this to outside
        AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
        parameters.setRedirectUri(exception.getRedirectUri());
        parameters.setStateKey(exception.getStateKey());
        parameters.setHttpMethod(HttpMethod.GET);
        parameters.setAuthorizationUrl(authorizationUrl);
        return parameters;
    }

    /**
     * Adds jawbone required parameters to authorization token requests.
     */
    private class RunkeeperTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("grant_type", resource.getGrantType());
            form.set("redirect_uri", "http://localhost:8080/authorize/" + getShimKey() + "/callback");
        }
    }
}
