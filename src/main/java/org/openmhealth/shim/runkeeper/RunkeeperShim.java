package org.openmhealth.shim.runkeeper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.openmhealth.shim.*;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
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

    public RunkeeperShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                         AccessParametersRepo accessParametersRepo) {
        super(authorizationRequestParametersRepo, accessParametersRepo);
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getClientSecret() {
        return RUNKEEPER_CLIENT_SECRET;
    }

    @Override
    public String getClientId() {
        return RUNKEEPER_CLIENT_ID;
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
        return RUNKEEPER_SCOPES;
    }


    public enum RunkeeperDataType implements ShimDataType {

        ACTIVITY("application/vnd.com.runkeeper.FitnessActivityFeed+json",
            "fitnessActivities",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    return ShimDataResponse.empty();
                }
            }),

        WEIGHT(
            "application/vnd.com.runkeeper.WeightSetFeed+json",
            "weight",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext ctxt)
                    throws IOException {
                    return ShimDataResponse.empty();
                }
            });

        private String dataTypeHeader;

        private String endPointUrl;

        private JsonDeserializer<ShimDataResponse> normalizer;

        RunkeeperDataType(String dataTypeHeader, String endPointUrl,
                          JsonDeserializer<ShimDataResponse> normalizer) {
            this.dataTypeHeader = dataTypeHeader;
            this.endPointUrl = endPointUrl;
            this.normalizer = normalizer;
        }

        @Override
        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return normalizer;
        }

        public String getDataTypeHeader() {
            return dataTypeHeader;
        }

        public String getEndPointUrl() {
            return endPointUrl;
        }
    }


    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new RunkeeperTokenRequestEnhancer());
        return provider;
    }

    @Override
    public ShimDataRequest getTriggerDataRequest() {
        ShimDataRequest shimDataRequest = new ShimDataRequest();
        shimDataRequest.setDataTypeKey(RunkeeperDataType.ACTIVITY.toString());
        shimDataRequest.setNumToReturn(1l);
        return shimDataRequest;
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {

        String dataTypeKey = shimDataRequest.getDataTypeKey().trim().toUpperCase();

        RunkeeperDataType runkeeperDataType;
        try {
            runkeeperDataType = RunkeeperDataType.valueOf(dataTypeKey);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + dataTypeKey + " in shimDataRequest, cannot retrieve data.");
        }

        String urlRequest = DATA_URL;
        urlRequest += "/" + runkeeperDataType.getEndPointUrl() + "?";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", runkeeperDataType.getDataTypeHeader());

        long numToSkip = 0;
        long numToReturn = 50;

        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.AUGUST, 10);
        Date endDate = new Date(cal.getTimeInMillis());
        cal.add(Calendar.DATE, -11);
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

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<byte[]> response = restTemplate.exchange(
            urlRequest,
            HttpMethod.GET,
            new HttpEntity<byte[]>(headers),
            byte[].class);

        JsonNode json = null;
        try {
            json = objectMapper.readTree(response.getBody());
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
            + "&redirect_uri=http://localhost:8080/authorize/" + getShimKey() + "/callback";
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
