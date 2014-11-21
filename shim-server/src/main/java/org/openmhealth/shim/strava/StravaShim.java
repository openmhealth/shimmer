/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.strava;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.build.ActivityBuilder;
import org.openmhealth.schema.pojos.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

import static org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit.m;

/**
 * Encapsulates parameters specific to strava api.
 *
 * @author Pedro Sampaio
 */
public class StravaShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "strava";

    private static final String DATA_URL = "https://www.strava.com/api/v3/";

    private static final String AUTHORIZE_URL = "https://www.strava.com/oauth/authorize";

    private static final String TOKEN_URL = "https://www.strava.com/oauth/token";

    private StravaConfig config;

    public static final ArrayList<String> STRAVA_SCOPES =
        new ArrayList<String>(Arrays.asList("public"));

    public StravaShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                         AccessParametersRepo accessParametersRepo,
                         ShimServerConfig shimServerConfig1,
                         StravaConfig stravaConfig) {
        super(authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig1);
        this.config = stravaConfig;
    }

    @Override
    public String getLabel() {
        return "Strava";
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
        return STRAVA_SCOPES;
    }

    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new StravaTokenRequestEnhancer());
        return provider;
    }

    @Override
    public ShimDataRequest getTriggerDataRequest() {
        ShimDataRequest shimDataRequest = new ShimDataRequest();
        shimDataRequest.setDataTypeKey(StravaDataTypes.ACTIVITY.toString());
        shimDataRequest.setNumToReturn(1l);
        return shimDataRequest;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return new StravaDataTypes[]{
            StravaDataTypes.ACTIVITY};
    }

    //Example: 2014-11-22T22:11:00Z
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z");

    public enum StravaDataTypes implements ShimDataType {

        ACTIVITY("athlete/activities", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                                                DeserializationContext deserializationContext)
                throws IOException {
                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                String rawJson = responseNode.toString();

                List<Activity> activities = new ArrayList<>();

                JsonPath workoutsPath = JsonPath.compile("$.[*]");
                List<Object> strvWorkouts = JsonPath.read(rawJson, workoutsPath.getPath());
                if (CollectionUtils.isEmpty(strvWorkouts)) {
                    return ShimDataResponse.result(StravaShim.SHIM_KEY, null);
                }
                
                ObjectMapper mapper = new ObjectMapper();
                for (Object rawWorkout : strvWorkouts) {
                    JsonNode strvWorkout = mapper.readTree(((JSONObject) rawWorkout).toJSONString());

                    DateTime startTime =
                            dateFormatter.withZone( DateTimeZone.UTC )
                                .parseDateTime(strvWorkout.get("start_date").asText());

                    Activity activity = new ActivityBuilder()
                        .setActivityName(strvWorkout.get("name").asText())
                        .setDistance(
                            strvWorkout.get("distance").asDouble(), m)
                        .withStartAndDuration(
                            startTime,
                            strvWorkout.get("moving_time").asDouble(),
                            DurationUnitValue.DurationUnit.sec).build();

                    activities.add(activity);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(Activity.SCHEMA_ACTIVITY, activities);
                return ShimDataResponse.result(StravaShim.SHIM_KEY, results);
            }
        });

        private String endPoint;

        private JsonDeserializer<ShimDataResponse> normalizer;

        StravaDataTypes(String endPoint, JsonDeserializer<ShimDataResponse> normalizer) {
            this.endPoint = endPoint;
            this.normalizer = normalizer;
        }

        @Override
        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return normalizer;
        }

        public String getEndPoint() {
            return endPoint;
        }
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {
        String urlRequest = DATA_URL;

        final StravaDataTypes stravaDataType;
        try {
            stravaDataType = StravaDataTypes.valueOf(
                shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        urlRequest += stravaDataType.getEndPoint() + "?";

        long numToReturn = 100;
        if (shimDataRequest.getNumToReturn() != null) {
            numToReturn = shimDataRequest.getNumToReturn();
        }

        DateTime today = new DateTime();

        DateTime startDate = shimDataRequest.getStartDate() == null ?
            today.minusDays(1) : shimDataRequest.getStartDate();
        long startTimeTs = startDate.toDate().getTime() / 1000;

        DateTime endDate = shimDataRequest.getEndDate() == null ?
            today.plusDays(1) : shimDataRequest.getEndDate();
        long endTimeTs = endDate.toDate().getTime() / 1000;

        urlRequest += "&after=" + startTimeTs;
        urlRequest += "&before=" + endTimeTs;
        urlRequest += "&per_page=" + numToReturn;

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(urlRequest, byte[].class);
        JsonNode json = null;
        try {
            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, stravaDataType.getNormalizer());
                objectMapper.registerModule(module);
                return new ResponseEntity<>(
                    objectMapper.readValue(responseEntity.getBody(), ShimDataResponse.class), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                    ShimDataResponse.result(StravaShim.SHIM_KEY, objectMapper.readTree(responseEntity.getBody())), HttpStatus.OK);
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
            + "?state=" + exception.getStateKey()
            + "&client_id=" + resource.getClientId()
            + "&scope=" + StringUtils.collectionToDelimitedString(resource.getScope(), " ")
            + "&response_type=code"
            + "&approval_prompt=force"
            + "&redirect_uri=" + getCallbackUrl();

        AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
        parameters.setRedirectUri(exception.getRedirectUri());
        parameters.setStateKey(exception.getStateKey());
        parameters.setHttpMethod(HttpMethod.GET);
        parameters.setAuthorizationUrl(authorizationUrl);
        return parameters;
    }

    /**
     * Adds strava required parameters to authorization token requests.
     */
    private class StravaTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("grant_type", resource.getGrantType());
            form.set("redirect_uri", getCallbackUrl());
        }
    }
}
