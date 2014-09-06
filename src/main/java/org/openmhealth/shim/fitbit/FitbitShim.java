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

package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.build.*;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

import static org.openmhealth.schema.pojos.generic.DurationUnitValue.*;
import static org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit.*;

/**
 * @author Danilo Bonilla
 */
public class FitbitShim extends OAuth1ShimBase {

    public static final String SHIM_KEY = "fitbit";

    private static final String DATA_URL = "https://api.fitbit.com";

    private static final String REQUEST_TOKEN_URL = "https://api.fitbit.com/oauth/request_token";

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth/authenticate";

    private static final String TOKEN_URL = "https://api.fitbit.com/oauth/access_token";

    private FitbitConfig config;

    public FitbitShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                      ShimServerConfig shimServerConfig,
                      FitbitConfig fitbitConfig) {
        super(authorizationRequestParametersRepo, shimServerConfig);
        this.config = fitbitConfig;
    }

    @Override
    public List<String> getScopes() {
        return null; //noop!
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
    public String getBaseRequestTokenUrl() {
        return REQUEST_TOKEN_URL;
    }

    @Override
    public String getBaseAuthorizeUrl() {
        return AUTHORIZE_URL;
    }

    @Override
    public String getBaseTokenUrl() {
        return TOKEN_URL;
    }

    protected HttpMethod getRequestTokenMethod() {
        return HttpMethod.POST;
    }

    protected HttpMethod getAccessTokenMethod() {
        return HttpMethod.POST;
    }

    private static DateTimeFormatter formatterMins =
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    public ShimDataType[] getShimDataTypes() {
        return FitbitDataType.values();
    }

    public enum FitbitDataType implements ShimDataType {

        WEIGHT(
            "body/log/weight",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BodyWeight> bodyWeights = new ArrayList<>();
                    JsonPath bodyWeightsPath = JsonPath.compile("$result.content.weight[*]");

                    List<Object> fbWeights = JsonPath.read(rawJson, bodyWeightsPath.getPath());
                    if (CollectionUtils.isEmpty(fbWeights)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : fbWeights) {
                        JsonNode fbWeight = mapper.readTree(((JSONObject) fva).toJSONString());

                        String dateStr = fbWeight.get("date").asText();
                        dateStr += fbWeight.get("time") != null ? "T" + fbWeight.get("time").asText() : "";

                        DateTime dateTimeWhen = new DateTime(dateStr);
                        BodyWeight bodyWeight = new BodyWeightBuilder()
                            .setWeight(
                                fbWeight.get("weight").asText(),
                                MassUnitValue.MassUnit.kg.toString())
                            .setTimeTaken(dateTimeWhen).build();

                        bodyWeights.add(bodyWeight);
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BodyWeight.SCHEMA_BODY_WEIGHT, bodyWeights);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        HEART(
            "heart",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<HeartRate> heartRates = new ArrayList<>();
                    JsonPath heartPath = JsonPath.compile("$.result.content.heart[*]");

                    String dateString = JsonPath.read(rawJson, "$.result.date").toString();

                    List<Object> fbHearts = JsonPath.read(rawJson, heartPath.getPath());
                    if (CollectionUtils.isEmpty(fbHearts)) {
                        return ShimDataResponse.result(null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : fbHearts) {
                        JsonNode fbHeart = mapper.readTree(((JSONObject) fva).toJSONString());

                        String heartDate = dateString;
                        if (fbHeart.get("time") != null) {
                            heartDate += "T" + fbHeart.get("time").asText();
                        }

                        heartRates.add(new HeartRateBuilder()
                            .withRate(fbHeart.get("heartRate").asInt())
                            .withTimeTaken(new DateTime(heartDate)).build());
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(HeartRate.SCHEMA_HEART_RATE, heartRates);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        BLOOD_PRESSURE(
            "bp",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BloodPressure> bloodPressures = new ArrayList<>();
                    JsonPath bpPath = JsonPath.compile("$.result.content.bp[*]");

                    String dateString = JsonPath.read(rawJson, "$.result.date").toString();

                    List<Object> fbBloodPressures = JsonPath.read(rawJson, bpPath.getPath());
                    if (CollectionUtils.isEmpty(fbBloodPressures)) {
                        return ShimDataResponse.result(null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : fbBloodPressures) {
                        JsonNode fbBp = mapper.readTree(((JSONObject) fva).toJSONString());

                        String bpDate = dateString;
                        if (fbBp.get("time") != null) {
                            bpDate += "T" + fbBp.get("time").asText();
                        }

                        bloodPressures.add(new BloodPressureBuilder()
                            .setTimeTaken(new DateTime(bpDate))
                            .setValues(
                                new BigDecimal(fbBp.get("systolic").asText()),
                                new BigDecimal(fbBp.get("diastolic").asText())
                            ).build());
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BloodPressure.SCHEMA_BLOOD_PRESSURE, bloodPressures);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        BLOOD_GLUCOSE(
            "glucose",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BloodGlucose> bloodGlucoses = new ArrayList<>();
                    JsonPath bpPath = JsonPath.compile("$.result.content.glucose[*]");

                    String dateString = JsonPath.read(rawJson, "$.result.date").toString();

                    List<Object> fbBloodPressures = JsonPath.read(rawJson, bpPath.getPath());
                    if (CollectionUtils.isEmpty(fbBloodPressures)) {
                        return ShimDataResponse.result(null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : fbBloodPressures) {
                        JsonNode fbBp = mapper.readTree(((JSONObject) fva).toJSONString());

                        String bpDate = dateString;
                        if (fbBp.get("time") != null) {
                            bpDate += "T" + fbBp.get("time").asText();
                        }

                        bloodGlucoses.add(new BloodGlucoseBuilder()
                            .setTimeTaken(new DateTime(bpDate))
                            .setMgdLValue(new BigDecimal(fbBp.get("glucose").asText())).build());
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BloodGlucose.SCHEMA_BLOOD_GLUCOSE, bloodGlucoses);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        STEPS("activities/steps", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                                                DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                String rawJson = responseNode.toString();

                List<StepCount> steps = new ArrayList<>();
                JsonPath stepsPath = JsonPath.compile("$.[*].result.content[*]");

                Object oneMinStepEntries = JsonPath.read(rawJson, stepsPath.getPath());

                if (oneMinStepEntries == null) {
                    return ShimDataResponse.empty();
                }

                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                ObjectMapper mapper = new ObjectMapper();

                /**
                 * Determine if many items were returned or just one
                 * and cast appropriately.
                 */
                ArrayNode nodes;
                String jsonString;
                if (oneMinStepEntries instanceof JSONArray) {
                    jsonString = ((JSONArray) oneMinStepEntries).toJSONString();
                } else {
                    jsonString = "[" +
                        ((JSONObject) oneMinStepEntries).toJSONString() + "]";
                }
                nodes = (ArrayNode) mapper.readTree(jsonString);

                for (Object node1 : nodes) {
                    JsonNode fbStepNode = (JsonNode) node1;

                    String dateString =
                        (fbStepNode.get("activities-steps")).get(0).get("dateTime").asText();

                    ArrayNode dataset = (ArrayNode)
                        fbStepNode.get("activities-steps-intraday").get("dataset");

                    for (JsonNode stepMinute : dataset) {
                        if (stepMinute.get("value").asInt() > 0) {
                            steps.add(new StepCountBuilder()
                                .withStartAndDuration(
                                    formatter.parseDateTime(
                                        dateString + " " + stepMinute.get("time").asText()),
                                    1d, DurationUnit.min
                                ).setSteps(stepMinute.get("value").asInt())
                                .build());
                        }
                    }
                }
                Map<String, Object> results = new HashMap<>();
                results.put(StepCount.SCHEMA_STEP_COUNT, steps);
                return ShimDataResponse.result(results);
            }
        }),

        ACTIVITY(
            "activities",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext deserializationContext)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<Activity> activities = new ArrayList<>();

                    JsonPath activityPath = JsonPath.compile("$.result.content.activities[*]");

                    final List<Object> fitbitActivities = JsonPath.read(rawJson, activityPath.getPath());
                    if (CollectionUtils.isEmpty(fitbitActivities)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : fitbitActivities) {
                        final JsonNode fitbitActivity = mapper.readTree(((JSONObject) fva).toJSONString());

                        String dateString = fitbitActivity.get("startDate").asText()
                            + (fitbitActivity.get("startTime") != null ?
                            " " + fitbitActivity.get("startTime").asText() : "");

                        DateTime startTime = formatterMins.parseDateTime(dateString);

                        Activity activity = new ActivityBuilder()
                            .setActivityName(fitbitActivity.get("activityParentName").asText())
                            .setDistance(fitbitActivity.get("distance").asDouble(), m)
                            .withStartAndDuration(
                                startTime, fitbitActivity.get("duration").asDouble(), DurationUnit.ms)
                            .build();

                        activities.add(activity);
                    }

                    Map<String, Object> results = new HashMap<>();
                    results.put(Activity.SCHEMA_ACTIVITY, activities);
                    return ShimDataResponse.result(results);
                }
            }
        );

        private String endPoint;

        private JsonDeserializer<ShimDataResponse> normalizer;

        FitbitDataType(String endPoint, JsonDeserializer<ShimDataResponse> normalizer) {
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

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {
        AccessParameters accessParameters = shimDataRequest.getAccessParameters();
        String accessToken = accessParameters.getAccessToken();
        String tokenSecret = accessParameters.getTokenSecret();

        FitbitDataType fitbitDataType;
        try {
            fitbitDataType = FitbitDataType.valueOf(shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        /***
         * Setup default date parameters
         */
        DateTime today =
            dayFormatter.parseDateTime(new DateTime().toString(dayFormatter)); //ensure beginning of today

        DateTime startDate = shimDataRequest.getStartDate() == null ?
            today.minusDays(1) : shimDataRequest.getStartDate();

        DateTime endDate = shimDataRequest.getEndDate() == null ?
            today.plusDays(1) : shimDataRequest.getEndDate();

        DateTime currentDate = startDate;

        /**
         * Fitbit's API limits you to making a request for each given day
         * of data. Thus we make a request for each day in the submitted time
         * range and then aggregate the response based on the normalization parameter.
         */
        List<ShimDataResponse> dayResponses = new ArrayList<>();
        while (currentDate.toDate().before(endDate.toDate()) ||
            currentDate.toDate().equals(endDate.toDate())) {
            dayResponses.add(getDaysData(currentDate, fitbitDataType,
                shimDataRequest.getNormalize(), accessToken, tokenSecret));
            currentDate = currentDate.plusDays(1);
        }
        return shimDataRequest.getNormalize() ?
            aggregateNormalized(dayResponses) : aggregateIntoList(dayResponses);
    }

    /**
     * Each 'dayResponse', when normalized, will have a type->list[objects] for the day.
     * So we collect each daily map to create an aggregate map of the full
     * time range.
     */
    @SuppressWarnings("unchecked")
    private ShimDataResponse aggregateNormalized(List<ShimDataResponse> dayResponses) {
        if (CollectionUtils.isEmpty(dayResponses)) {
            return ShimDataResponse.empty();
        }
        Map<String, Collection<Object>> aggregateMap = new HashMap<>();
        for (ShimDataResponse dayResponse : dayResponses) {
            if (dayResponse.getBody() != null) {
                Map<String, Collection<Object>> dayMap =
                    (Map<String, Collection<Object>>) dayResponse.getBody();
                for (String typeKey : dayMap.keySet()) {
                    if (!aggregateMap.containsKey(typeKey)) {
                        aggregateMap.put(typeKey, new ArrayList<>());
                    }
                    aggregateMap.get(typeKey).addAll(dayMap.get(typeKey));
                }
            }
        }
        return aggregateMap.size() == 0 ?
            ShimDataResponse.empty() : ShimDataResponse.result(aggregateMap);
    }

    /**
     * Combines all response bodies for each day into a single response.
     *
     * @param dayResponses - daily responses to combine.
     * @return - Combined shim response.
     */
    private ShimDataResponse aggregateIntoList(List<ShimDataResponse> dayResponses) {
        if (CollectionUtils.isEmpty(dayResponses)) {
            return ShimDataResponse.empty();
        }
        List<Object> responses = new ArrayList<>();
        for (ShimDataResponse dayResponse : dayResponses) {
            if (dayResponse.getBody() != null) {
                responses.add(dayResponse.getBody());
            }
        }
        return responses.size() == 0 ? ShimDataResponse.empty() :
            ShimDataResponse.result(responses);
    }

    private ShimDataResponse getDaysData(DateTime dateTime,
                                         FitbitDataType fitbitDataType,
                                         boolean normalize,
                                         String accessToken, String tokenSecret) throws ShimException {

        String dateString = dateTime.toString(dayFormatter);

        String endPointUrl = DATA_URL;
        endPointUrl += "/1/user/-/"
            + fitbitDataType.getEndPoint() + "/date/" + dateString
            + (fitbitDataType == FitbitDataType.STEPS ? "/1d/1min" : "") //special setting for time series
            + ".json";

        HttpRequestBase dataRequest =
            OAuth1Utils.getSignedRequest(HttpMethod.GET,
                endPointUrl, getClientId(), getClientSecret(), accessToken, tokenSecret, null);

        HttpResponse response;
        try {
            response = httpClient.execute(dataRequest);
            HttpEntity responseEntity = response.getEntity();

            /**
             * The fitbit API's system works by retrieving each day's
             * data. The date captured is not returned in the data from fitbit because
             * it's implicit so we create a JSON wrapper that includes it.
             */
            StringWriter writer = new StringWriter();
            IOUtils.copy(responseEntity.getContent(), writer);
            String jsonContent = "{\"result\": {\"date\": \"" + dateString + "\" " +
                ",\"content\": " + writer.toString() + "}}";

            ObjectMapper objectMapper = new ObjectMapper();
            if (normalize) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, fitbitDataType.getNormalizer());
                objectMapper.registerModule(module);
                return objectMapper.readValue(jsonContent, ShimDataResponse.class);
            } else {
                return ShimDataResponse.result(objectMapper.readTree(jsonContent));
            }
        } catch (IOException e) {
            throw new ShimException("Could not fetch data", e);
        } finally {
            dataRequest.releaseConnection();
        }
    }
}
