package org.openmhealth.shim.moves;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.shim.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.*;


/**
 * Created by Cheng-Kang Hsieh on 3/3/15.
 */
public class MovesShim extends OAuth2ShimBase{
    private static final Logger log = LoggerFactory.getLogger(MovesShim.class);

    public static final String SHIM_KEY = "moves";

    private static final String DATA_URL = "https://api.moves-app.com/api/1.1";

    private static final String AUTHORIZE_URL = "https://api.moves-app.com/oauth/v1/authorize";

    private static final String TOKEN_URL = "https://api.moves-app.com/oauth/v1/access_token";

    private MovesConfig config;

    public static final ArrayList<String> MOVES_SCOPES =
            new ArrayList<>(Arrays.asList(
                    "activity", "location"
            ));

    public MovesShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                         AccessParametersRepo accessParametersRepo,
                         ShimServerConfig shimServerConfig1,
                         MovesConfig movesConfig) {
        super(authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig1);
        this.config = movesConfig;
    }

    @Override
    public String getLabel() {
        return "Moves";
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getClientSecret() {
        return config.getClientSecret();
    }

    @Override
    public String getClientId() {
        return config.getClientId();
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
        return MOVES_SCOPES;
    }

    //Example: Wed, 6 Aug 2014 04:49:00
    //private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("EEE, d MMM yyyy HH:mm:ss");

    public enum MovesDataType implements ShimDataType {

        STORYLINE("/user/storyline/daily",
                new JsonDeserializer<ShimDataResponse>() {
                    @Override
                    public ShimDataResponse deserialize(JsonParser jsonParser,
                                                        DeserializationContext ctxt)
                            throws IOException {
                throw new UnsupportedOperationException("Moves normalizer is not supported yet!");

                    }
                }),
        PROFILE("/user/profile",  new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                    DeserializationContext ctxt) throws IOException {
                throw new UnsupportedOperationException("Moves normalizer is not supported yet!");
            }
        }),
        SUMMARY("/user/summary/daily",
                        new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                    DeserializationContext ctxt)
            throws IOException {
                throw new UnsupportedOperationException("Moves normalizer is not supported yet!");

            }
        }),
        ACTIVITIES("/user/activities/daily",
                new JsonDeserializer<ShimDataResponse>() {
                    @Override
                    public ShimDataResponse deserialize(JsonParser jsonParser,
                                                        DeserializationContext ctxt)
                            throws IOException {
                        throw new UnsupportedOperationException("Moves normalizer is not supported yet!");

                    }
                }),
        PLACES("/user/places/daily",
                new JsonDeserializer<ShimDataResponse>() {
                    @Override
                    public ShimDataResponse deserialize(JsonParser jsonParser,
                                                        DeserializationContext ctxt)
                            throws IOException {
                        throw new UnsupportedOperationException("Moves normalizer is not supported yet!");

                    }
                });

        private String endPointUrl;

        private JsonDeserializer<ShimDataResponse> normalizer;

        MovesDataType(String endPointUrl,
                          JsonDeserializer<ShimDataResponse> normalizer) {
            this.endPointUrl = endPointUrl;
            this.normalizer = normalizer;
        }

        @Override
        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return normalizer;
        }

        public String getEndPointUrl() {
            return endPointUrl;
        }
    }


    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new MovesTokenRequestEnhancer());
        return provider;
    }

    @Override
    public ShimDataRequest getTriggerDataRequest() {
        ShimDataRequest shimDataRequest = new ShimDataRequest();
        shimDataRequest.setDataTypeKey(MovesDataType.PROFILE.toString());
        shimDataRequest.setNumToReturn(1l);
        return shimDataRequest;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return MovesDataType.values();
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {

        String dataTypeKey = shimDataRequest.getDataTypeKey().trim().toUpperCase();

        MovesDataType movesDataType;
        try {
            movesDataType = MovesDataType.valueOf(dataTypeKey);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                    + dataTypeKey + " in shimDataRequest, cannot retrieve data.");
        }

        String urlRequest = DATA_URL;
        urlRequest += "/" + movesDataType.getEndPointUrl();
        String urlParams;
        if(movesDataType.equals(MovesDataType.PROFILE)){
            urlParams = "";
        }else {
            /***
             * Setup default date parameters
             */
            final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            DateTime today = new DateTime();

            DateTime startDate = shimDataRequest.getStartDate() == null ?
                    today.minusDays(1) : shimDataRequest.getStartDate();
            String dateStart = startDate.toString(formatter);

            DateTime endDate = shimDataRequest.getEndDate() == null ?
                    today : shimDataRequest.getEndDate();
            String dateEnd = endDate.toString(formatter);

            urlParams = "&trackPoints=true";
            urlParams += "&from=" + dateStart;
            urlParams += "&to=" + dateEnd;
            urlParams = urlParams.substring(1, urlParams.length());
        }

        urlRequest += "?" + urlParams;
        ObjectMapper objectMapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + restTemplate.getAccessToken().getValue());
        ResponseEntity<byte[]> response = restTemplate.exchange(
                urlRequest,
                HttpMethod.GET,
                new HttpEntity<byte[]>(headers),
                byte[].class);

        try {
            if (shimDataRequest.getNormalize()) {
                throw new UnsupportedOperationException("Moves normalizer is not supported yet!");
            } else {
                return new ResponseEntity<>(
                        ShimDataResponse.result(MovesShim.SHIM_KEY, objectMapper.readTree(response.getBody())), HttpStatus.OK);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not read response data.");
        }
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
                + "&redirect_uri=" + getCallbackUrl()
                + "&scope=" + "activity location";

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
    private class MovesTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("grant_type", resource.getGrantType());
            form.set("redirect_uri", getCallbackUrl());
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
    }
}
