/*
 * Copyright 2014 Open mHealth
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

package org.openmhealth.shim.googlefit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.build.ActivityBuilder;
import org.openmhealth.schema.pojos.build.BodyHeightBuilder;
import org.openmhealth.schema.pojos.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.build.HeartRateBuilder;
import org.openmhealth.schema.pojos.build.StepCountBuilder;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;
import org.openmhealth.shim.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Encapsulates parameters specific to the Google Fit API.
 *
 * @author Eric Jain
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.googlefit")
public class GoogleFitShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "googlefit";

    private static final String DATASET_URL = "https://www.googleapis.com/fitness/v1/users/me/dataSources/%s/datasets/%d-%d?limit=%d";

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth";

    private static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";

    public static final List<String> GOOGLE_FIT_SCOPES = Arrays.asList(
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/fitness.activity.read",
        "https://www.googleapis.com/auth/fitness.body.read"
    );

    @Autowired
    public GoogleFitShim(ApplicationAccessParametersRepo applicationParametersRepo,
                       AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                       AccessParametersRepo accessParametersRepo,
                       ShimServerConfig shimServerConfig) {
        super(applicationParametersRepo, authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig);
    }

    @Override
    public String getLabel() {
        return "Google Fit";
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
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
        return GOOGLE_FIT_SCOPES;
    }

    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new GoogleAuthorizationCodeAccessTokenProvider();
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return new GoogleFitDataTypes[]{
            GoogleFitDataTypes.ACTIVITY,
            GoogleFitDataTypes.BODY_HEIGHT,
            GoogleFitDataTypes.BODY_WEIGHT,
            GoogleFitDataTypes.HEART_RATE,
            GoogleFitDataTypes.STEP_COUNT};
    }

    public enum GoogleFitDataTypes implements ShimDataType {

        ACTIVITY("derived:com.google.activity.segment:com.google.android.gms:merge_activity_segments", Activity.SCHEMA_ACTIVITY, new DataPointNormalizer() {

            private final ImmutableMap<Integer, String> activityNames = ImmutableMap.<Integer, String>builder()
                .put(9, "Aerobics")
                .put(10, "Badminton")
                .put(11, "Baseball")
                .put(12, "Basketball")
                .put(13, "Biathlon")
                .put(1, "Biking")
                .put(14, "Handbiking")
                .put(15, "Mountain biking")
                .put(16, "Road biking")
                .put(17, "Spinning")
                .put(18, "Stationary biking")
                .put(19, "Utility biking")
                .put(20, "Boxing")
                .put(21, "Calisthenics")
                .put(22, "Circuit training")
                .put(23, "Cricket")
                .put(106, "Curling")
                .put(24, "Dancing")
                .put(102, "Diving")
                .put(25, "Elliptical")
                .put(103, "Ergometer")
                .put(26, "Fencing")
                .put(27, "Football (American)")
                .put(28, "Football (Australian)")
                .put(29, "Football (Soccer)")
                .put(30, "Frisbee")
                .put(31, "Gardening")
                .put(32, "Golf")
                .put(33, "Gymnastics")
                .put(34, "Handball")
                .put(35, "Hiking")
                .put(36, "Hockey")
                .put(37, "Horseback riding")
                .put(38, "Housework")
                .put(104, "Ice skating")
                .put(0, "In vehicle")
                .put(39, "Jumping rope")
                .put(40, "Kayaking")
                .put(41, "Kettlebell training")
                .put(42, "Kickboxing")
                .put(43, "Kitesurfing")
                .put(44, "Martial arts")
                .put(45, "Meditation")
                .put(46, "Mixed martial arts")
                .put(2, "On foot")
                .put(47, "P90X exercises")
                .put(48, "Paragliding")
                .put(49, "Pilates")
                .put(50, "Polo")
                .put(51, "Racquetball")
                .put(52, "Rock climbing")
                .put(53, "Rowing")
                .put(54, "Rowing machine")
                .put(55, "Rugby")
                .put(8, "Running")
                .put(56, "Jogging")
                .put(57, "Running on sand")
                .put(58, "Running (treadmill)")
                .put(59, "Sailing")
                .put(60, "Scuba diving")
                .put(61, "Skateboarding")
                .put(62, "Skating")
                .put(63, "Cross skating")
                .put(105, "Indoor skating")
                .put(64, "Inline skating (rollerblading)")
                .put(65, "Skiing")
                .put(66, "Back-country skiing")
                .put(67, "Cross-country skiing")
                .put(68, "Downhill skiing")
                .put(69, "Kite skiing")
                .put(70, "Roller skiing")
                .put(71, "Sledding")
                .put(72, "Sleeping")
                .put(73, "Snowboarding")
                .put(74, "Snowmobile")
                .put(75, "Snowshoeing")
                .put(76, "Squash")
                .put(77, "Stair climbing")
                .put(78, "Stair-climbing machine")
                .put(79, "Stand-up paddleboarding")
                .put(3, "Still (not moving)")
                .put(80, "Strength training")
                .put(81, "Surfing")
                .put(82, "Swimming")
                .put(84, "Swimming (open water)")
                .put(83, "Swimming (swimming pool)")
                .put(85, "Table tenis (ping pong)")
                .put(86, "Team sports")
                .put(87, "Tennis")
                .put(5, "Tilting (sudden device gravity change)")
                .put(88, "Treadmill (walking or running)")
                .put(4, "Unknown (unable to detect activity)")
                .put(89, "Volleyball")
                .put(90, "Volleyball (beach)")
                .put(91, "Volleyball (indoor)")
                .put(92, "Wakeboarding")
                .put(7, "Walking")
                .put(93, "Walking (fitness)")
                .put(94, "Nording walking")
                .put(95, "Walking (treadmill)")
                .put(96, "Waterpolo")
                .put(97, "Weightlifting")
                .put(98, "Wheelchair")
                .put(99, "Windsurfing")
                .put(100, "Yoga")
                .put(101, "Zumba")
                .put(108, "Other") // not documented
                .build();

            @Override
            DataPoint getDataPoint(JsonNode node) {
                Activity dataPoint = null;
                int activityCode = node.get("value").get(0).get("intVal").intValue();
                String activityName = activityNames.get(activityCode);
                if (activityName != null) {
                    dataPoint = new ActivityBuilder().setActivityName(activityName).build();
                    setTimeTaken(dataPoint, node);
                }
                return dataPoint;
            }
        }),

        BODY_HEIGHT("derived:com.google.height:com.google.android.gms:merge_height", BodyHeight.SCHEMA_BODY_HEIGHT, new DataPointNormalizer() {

            @Override
            DataPoint getDataPoint(JsonNode node) {
                BodyHeight dataPoint = new BodyHeightBuilder()
                    .setHeight(
                        node.get("value").get(0).get("fpVal").asText(),
                        LengthUnitValue.LengthUnit.m.toString()).build();
                setTimeTaken(dataPoint, node);
                return dataPoint;
            }
        }),

        BODY_WEIGHT("derived:com.google.weight:com.google.android.gms:merge_weight", BodyWeight.SCHEMA_BODY_WEIGHT, new DataPointNormalizer() {

            @Override
            DataPoint getDataPoint(JsonNode node) {
                BodyWeight dataPoint = new BodyWeightBuilder()
                    .setWeight(
                        node.get("value").get(0).get("fpVal").asText(),
                        MassUnitValue.MassUnit.kg.toString()).build();
                setTimeTaken(dataPoint, node);
                return dataPoint;
            }
        }),

        HEART_RATE("derived:com.google.heart_rate.bpm:com.google.android.gms:merge_heart_rate_bpm", HeartRate.SCHEMA_HEART_RATE, new DataPointNormalizer() {

            @Override
            DataPoint getDataPoint(JsonNode node) {
                HeartRate dataPoint = new HeartRateBuilder()
                    .withRate(node.get("value").get(0).get("fpVal").intValue())
                    .build();
                setTimeTaken(dataPoint, node);
                return dataPoint;
            }
        }),

        STEP_COUNT("derived:com.google.step_count.delta:com.google.android.gms:merge_step_deltas", StepCount.SCHEMA_STEP_COUNT, new DataPointNormalizer() {

            @Override
            DataPoint getDataPoint(JsonNode node) {
                StepCount dataPoint = new StepCountBuilder()
                    .setSteps(node.get("value").get(0).get("intVal").intValue())
                    .build();
                setTimeTaken(dataPoint, node);
                return dataPoint;
            }
        });

        private final String streamId;
        private final String schemaId;
        private final DataPointNormalizer normalizer;

        GoogleFitDataTypes(String streamId, String schemaId, DataPointNormalizer normalizer) {
            this.streamId = streamId;
            this.schemaId = schemaId;
            this.normalizer = normalizer;
        }

        public String getStreamId() {
            return streamId;
        }

        public String getSchemaId() {
            return schemaId;
        }

        @Override
        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                    throws IOException {

                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);

                    if (responseNode.path("point").size() == 0) {
                        return ShimDataResponse.result(GoogleFitShim.SHIM_KEY, null);
                    }

                    List<DataPoint> dataPoints = new ArrayList<>();
                    for (JsonNode dataPointNode : responseNode.path("point")) {
                        DataPoint dataPoint = normalizer.getDataPoint(dataPointNode);
                        if (dataPoint != null) {
                            dataPoints.add(dataPoint);
                        }
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(schemaId, dataPoints);
                    return ShimDataResponse.result(GoogleFitShim.SHIM_KEY, results);
                }
            };
        }
    }

    private static abstract class DataPointNormalizer {

        abstract DataPoint getDataPoint(JsonNode node);

        static void setTimeTaken(BaseDataPoint dataPoint, JsonNode node) {
            DateTime begin = parseDateTime(node.get("startTimeNanos"), DateTimeZone.UTC);
            DateTime end = parseDateTime(node.get("endTimeNanos"), DateTimeZone.UTC);
            dataPoint.setEffectiveTimeFrame(toTimeFrame(begin, end));
        }

        static DateTime parseDateTime(JsonNode node, DateTimeZone dateTimeZone) {
            return new DateTime(node.asLong() / 1_000_000, dateTimeZone);
        }

        static TimeFrame toTimeFrame(DateTime begin, DateTime end) {
            return begin.isEqual(end) ? TimeFrame.withDateTime(begin) : TimeFrame.withTimeInterval(begin, end);
        }
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {
        final GoogleFitDataTypes googleFitDataType;
        try {
            googleFitDataType = GoogleFitDataTypes.valueOf(
                shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        long numToReturn = 100;
        if (shimDataRequest.getNumToReturn() != null) {
            numToReturn = shimDataRequest.getNumToReturn();
        }

        DateTime today = new DateTime();

        DateTime startDate = shimDataRequest.getStartDate() == null ?
            today.minusDays(1) : shimDataRequest.getStartDate();
        long startTimeTs = startDate.toDate().getTime() * 1_000_000;

        DateTime endDate = shimDataRequest.getEndDate() == null ?
            today.plusDays(1) : shimDataRequest.getEndDate();
        long endTimeTs = endDate.toDate().getTime() * 1_000_000;


        String urlRequest = String.format(DATASET_URL, googleFitDataType.getStreamId(), startTimeTs, endTimeTs, numToReturn);

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(urlRequest, byte[].class);
        try {
            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, googleFitDataType.getNormalizer());
                objectMapper.registerModule(module);
                return new ResponseEntity<>(
                    objectMapper.readValue(responseEntity.getBody(), ShimDataResponse.class), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                    ShimDataResponse.result(GoogleFitShim.SHIM_KEY, objectMapper.readTree(responseEntity.getBody())), HttpStatus.OK);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not read response data.");
        }
    }

    @Override
    protected String getAuthorizationUrl(UserRedirectRequiredException exception) {
        final OAuth2ProtectedResourceDetails resource = getResource();
        return exception.getRedirectUri()
            + "?state="
            + exception.getStateKey()
            + "&client_id="
            + resource.getClientId()
            + "&response_type=code"
            + "&access_type=offline"
            + "&approval_prompt=force" // required to get refresh tokens
            + "&scope=" + StringUtils.collectionToDelimitedString(resource.getScope(), " ")
            + "&redirect_uri=" + getCallbackUrl();
    }

    /**
     * Simple overrides to base spring class from oauth.
     */
    public class GoogleAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
        public GoogleAuthorizationCodeAccessTokenProvider() {
            this.setTokenRequestEnhancer(new GoogleTokenRequestEnhancer());
        }

        @Override
        protected HttpMethod getHttpMethod() {
            return HttpMethod.POST;
        }

        @Override
        public OAuth2AccessToken refreshAccessToken(
                OAuth2ProtectedResourceDetails resource,
                OAuth2RefreshToken refreshToken, AccessTokenRequest request)
                throws UserRedirectRequiredException,
                OAuth2AccessDeniedException {
            OAuth2AccessToken accessToken = super.refreshAccessToken(resource, refreshToken, request);
            // Google does not replace refresh tokens, so we need to hold on to the existing refresh token...
            if (accessToken.getRefreshToken() == null) {
                ((DefaultOAuth2AccessToken) accessToken).setRefreshToken(refreshToken);
            }
            return accessToken;
        }
    }

    /**
     * Adds parameters required by Google to authorization token requests.
     */
    private class GoogleTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            if (request.getStateKey() != null) {
                form.set("redirect_uri", getCallbackUrl());
            }
        }
    }
}
