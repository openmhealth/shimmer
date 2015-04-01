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

package org.openmhealth.shim.jawbone;

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
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.build.ActivityBuilder;
import org.openmhealth.schema.pojos.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.build.SleepDurationBuilder;
import org.openmhealth.schema.pojos.build.StepCountBuilder;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

import static org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit.*;
import static org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;

/**
 * Encapsulates parameters specific to the Jawbone API.
 *
 * @author Danilo Bonilla
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.jawbone")
public class JawboneShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "jawbone";

    private static final String DATA_URL = "https://jawbone.com/nudge/api/v.1.1/users/@me/";

    private static final String AUTHORIZE_URL = "https://jawbone.com/auth/oauth2/auth";

    private static final String TOKEN_URL = "https://jawbone.com/auth/oauth2/token";

    public static final List<String> JAWBONE_SCOPES = Arrays.asList(
        "extended_read", "weight_read", "cardiac_read", "meal_read", "move_read", "sleep_read");

    @Autowired
    public JawboneShim(ApplicationAccessParametersRepo applicationParametersRepo,
                       AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                       AccessParametersRepo accessParametersRepo,
                       ShimServerConfig shimServerConfig1) {
        super(applicationParametersRepo, authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig1);
    }

    @Override
    public String getLabel() {
        return "Jawbone UP";
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
        return JAWBONE_SCOPES;
    }

    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new JawboneAuthorizationCodeAccessTokenProvider();
    }

    @Override
    public ShimDataRequest getTriggerDataRequest() {
        ShimDataRequest shimDataRequest = new ShimDataRequest();
        shimDataRequest.setDataTypeKey(JawboneDataTypes.BODY.toString());
        shimDataRequest.setNumToReturn(1l);
        return shimDataRequest;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return new JawboneDataTypes[]{
            JawboneDataTypes.BODY, JawboneDataTypes.SLEEP, JawboneDataTypes.WORKOUTS, JawboneDataTypes.MOVES};
    }

    public enum JawboneDataTypes implements ShimDataType {

        BODY("body_events", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                                                DeserializationContext deserializationContext)
                throws IOException {
                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                String rawJson = responseNode.toString();

                List<BodyWeight> bodyWeights = new ArrayList<>();

                JsonPath bodyWeightsPath = JsonPath.compile("$.data.items[*]");
                List<Object> jbWeights = JsonPath.read(rawJson, bodyWeightsPath.getPath());
                if (CollectionUtils.isEmpty(jbWeights)) {
                    return ShimDataResponse.result(JawboneShim.SHIM_KEY, null);
                }
                ObjectMapper mapper = new ObjectMapper();
                for (Object rawWeight : jbWeights) {

                    JsonNode jbWeight = mapper.readTree(((JSONObject) rawWeight).toJSONString());

                    DateTimeZone dateTimeZone = DateTimeZone.UTC;
                    if (jbWeight.get("details") != null && jbWeight.get("details").get("tz") != null) {
                        dateTimeZone = DateTimeZone.forID(jbWeight.get("details").get("tz")
                            .asText().replaceAll(" ", "_"));
                    }

                    DateTime timeStamp = new DateTime(
                        jbWeight.get("time_created").asLong() * 1000, dateTimeZone);
                    timeStamp = timeStamp.toDateTime(DateTimeZone.UTC);

                    BodyWeight bodyWeight = new BodyWeightBuilder()
                        .setWeight(
                            jbWeight.get("weight").asText(),
                            MassUnitValue.MassUnit.kg.toString())
                        .setTimeTaken(timeStamp).build();

                    bodyWeights.add(bodyWeight);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(BodyWeight.SCHEMA_BODY_WEIGHT, bodyWeights);
                return ShimDataResponse.result(JawboneShim.SHIM_KEY, results);
            }
        }),

        SLEEP("sleeps", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                                                DeserializationContext deserializationContext)
                throws IOException {
                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                String rawJson = responseNode.toString();

                List<SleepDuration> sleepDurations = new ArrayList<>();

                JsonPath sleepsPath = JsonPath.compile("$.data.items[*]");
                List<Object> jbSleeps = JsonPath.read(rawJson, sleepsPath.getPath());
                if (CollectionUtils.isEmpty(jbSleeps)) {
                    return ShimDataResponse.result(JawboneShim.SHIM_KEY, null);
                }
                ObjectMapper mapper = new ObjectMapper();
                for (Object rawSleep : jbSleeps) {
                    JsonNode jbSleep = mapper.readTree(((JSONObject) rawSleep).toJSONString());

                    DateTimeZone dateTimeZone = DateTimeZone.UTC;
                    if (jbSleep.get("details") != null && jbSleep.get("details").get("tz") != null) {
                        dateTimeZone = DateTimeZone.forID(jbSleep.get("details").get("tz")
                            .asText().replaceAll(" ", "_"));
                    }

                    DateTime timeStamp = new DateTime(
                        jbSleep.get("time_created").asLong() * 1000, dateTimeZone);
                    timeStamp = timeStamp.toDateTime(DateTimeZone.UTC);

                    DateTime timeCompleted = new DateTime(
                        jbSleep.get("time_completed").asLong() * 1000, dateTimeZone);
                    timeCompleted = timeCompleted.toDateTime(DateTimeZone.UTC);

                    SleepDuration sleepDuration = new SleepDurationBuilder()
                        .withStartAndEndAndDuration(
                            timeStamp, timeCompleted,
                            jbSleep.get("details").get("duration").asDouble() / 60d,
                            SleepDurationUnitValue.Unit.min)
                        .build();

                    sleepDurations.add(sleepDuration);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(SleepDuration.SCHEMA_SLEEP_DURATION, sleepDurations);
                return ShimDataResponse.result(JawboneShim.SHIM_KEY, results);
            }
        }),

        WORKOUTS("workouts", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                                                DeserializationContext deserializationContext)
                throws IOException {
                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                String rawJson = responseNode.toString();

                List<Activity> activities = new ArrayList<>();

                JsonPath workoutsPath = JsonPath.compile("$.data.items[*]");
                List<Object> jbWorkouts = JsonPath.read(rawJson, workoutsPath.getPath());
                if (CollectionUtils.isEmpty(jbWorkouts)) {
                    return ShimDataResponse.result(JawboneShim.SHIM_KEY, null);
                }
                ObjectMapper mapper = new ObjectMapper();
                for (Object rawWorkout : jbWorkouts) {
                    JsonNode jbWorkout = mapper.readTree(((JSONObject) rawWorkout).toJSONString());

                    DateTimeZone dateTimeZone = DateTimeZone.UTC;
                    if (jbWorkout.get("details") != null && jbWorkout.get("details").get("tz") != null) {
                        dateTimeZone = DateTimeZone.forID(jbWorkout.get("details").get("tz")
                            .asText().replaceAll(" ", "_"));
                    }

                    DateTime timeStamp = new DateTime(
                        jbWorkout.get("time_created").asLong() * 1000, dateTimeZone);
                    timeStamp = timeStamp.toDateTime(DateTimeZone.UTC);

                    Activity activity = new ActivityBuilder()
                        .setActivityName(jbWorkout.get("title").asText())
                        .setDistance(jbWorkout.get("details").get("meters").asDouble(), LengthUnit.m)
                        .withStartAndDuration(
                            timeStamp, jbWorkout.get("details").get("time").asDouble(), sec)
                        .build();

                    activities.add(activity);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(Activity.SCHEMA_ACTIVITY, activities);
                return ShimDataResponse.result(JawboneShim.SHIM_KEY, results);
            }
        }),

        @SuppressWarnings("unchecked")
        MOVES("moves", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                                                DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                String rawJson = responseNode.toString();
                JsonPath stepsPath = JsonPath.compile("$.data.items[*]");

                List<Object> jbStepEntries = JsonPath.read(rawJson, stepsPath.getPath());

                if (CollectionUtils.isEmpty(jbStepEntries)) {
                    return ShimDataResponse.empty(JawboneShim.SHIM_KEY);
                }

                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHH");

                List<StepCount> stepCounts = new ArrayList<>();

                ObjectMapper mapper = new ObjectMapper();
                for (Object rawStepEntry : jbStepEntries) {
                    JsonNode jbStepEntry = mapper.readTree(((JSONObject) rawStepEntry).toJSONString());

                    DateTimeZone dateTimeZone = DateTimeZone.UTC;
                    if (jbStepEntry.get("details") != null && jbStepEntry.get("details").get("tz") != null) {
                        dateTimeZone = DateTimeZone.forID(jbStepEntry.get("details").get("tz")
                            .asText().replaceAll(" ", "_"));
                    }

                    JsonNode hourlyTotals = jbStepEntry.get("details").get("hourly_totals");
                    if (hourlyTotals == null) {
                        continue;
                    }
                    for (Iterator<Map.Entry<String, JsonNode>> iterator = hourlyTotals.fields(); iterator.hasNext(); ) {
                        Map.Entry<String, JsonNode> item = iterator.next();

                        String timestampStr = item.getKey();
                        JsonNode node = item.getValue();

                        DateTime dateTime = formatter.withZone(dateTimeZone).parseDateTime(timestampStr);
                        dateTime = dateTime.toDateTime(DateTimeZone.UTC);

                        if (node.get("steps").asInt() > 0) {
                            stepCounts.add(new StepCountBuilder()
                                .withStartAndDuration(
                                    dateTime, Double.parseDouble(node.get("active_time") + ""), sec)
                                .setSteps(node.get("steps").asInt()).build());
                        }
                    }
                }
                Map<String, Object> results = new HashMap<>();
                results.put(StepCount.SCHEMA_STEP_COUNT, stepCounts);
                return ShimDataResponse.result(JawboneShim.SHIM_KEY, results);

            }
        });

        private String endPoint;

        private JsonDeserializer<ShimDataResponse> normalizer;

        JawboneDataTypes(String endPoint, JsonDeserializer<ShimDataResponse> normalizer) {
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

        final JawboneDataTypes jawboneDataType;
        try {
            jawboneDataType = JawboneDataTypes.valueOf(
                shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        urlRequest += jawboneDataType.getEndPoint() + "?";

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

        urlRequest += "&start_time=" + startTimeTs;
        urlRequest += "&end_time=" + endTimeTs;
        urlRequest += "&limit=" + numToReturn;

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(urlRequest, byte[].class);
        JsonNode json = null;
        try {
            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, jawboneDataType.getNormalizer());
                objectMapper.registerModule(module);
                return new ResponseEntity<>(
                    objectMapper.readValue(responseEntity.getBody(), ShimDataResponse.class), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                    ShimDataResponse.result(JawboneShim.SHIM_KEY, objectMapper.readTree(responseEntity.getBody())), HttpStatus.OK);
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
            + "&scope=" + StringUtils.collectionToDelimitedString(resource.getScope(), " ")
            + "&redirect_uri=" + getCallbackUrl();
    }


    /**
     * Simple overrides to base spring class from oauth.
     */
    public class JawboneAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
        public JawboneAuthorizationCodeAccessTokenProvider() {
            this.setTokenRequestEnhancer(new JawboneTokenRequestEnhancer());
        }

        @Override
        protected HttpMethod getHttpMethod() {
            return HttpMethod.GET;
        }
    }

    /**
     * Adds jawbone required parameters to authorization token requests.
     */
    private class JawboneTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
        }
    }
}
