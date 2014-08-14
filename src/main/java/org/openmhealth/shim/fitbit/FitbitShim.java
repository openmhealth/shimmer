package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.build.*;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

public class FitbitShim extends OAuth1ShimBase {
    public static final String SHIM_KEY = "fitbit";

    private static final String DATA_URL = "https://api.fitbit.com";

    private static final String REQUEST_TOKEN_URL = "https://api.fitbit.com/oauth/request_token";

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth/authenticate";

    private static final String TOKEN_URL = "https://api.fitbit.com/oauth/access_token";

    public static final String FITBIT_CLIENT_ID = "7da3c2e5e74d4492ab6bb3286fc32c6b";

    public static final String FITBIT_CLIENT_SECRET = "455a383f80de45d6a4f9b09e841da1f4";

    public FitbitShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                      ShimServerConfig shimServerConfig) {
        super(authorizationRequestParametersRepo, shimServerConfig);
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
        return FITBIT_CLIENT_SECRET;
    }

    @Override
    public String getClientId() {
        return FITBIT_CLIENT_ID;
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
                    return ShimDataResponse.result(bodyWeights);
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
                            .setRate(fbHeart.get("heartRate").asText())
                            .setTimeTaken(new DateTime(heartDate)).build());

                    }
                    Map<String, Object> results = new HashMap<>();
                    //todo: Change this to constants driven elements!
                    results.put("heart-rate", heartRates);
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
                    //todo: Change this to constants driven elements!
                    results.put("blood-pressure", bloodPressures);
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
                            .setValue(fbBp.get("glucose").asText()).build());
                    }
                    Map<String, Object> results = new HashMap<>();
                    //todo: Change this to constants driven elements!
                    results.put("blood-glucose", bloodGlucoses);
                    return ShimDataResponse.result(results);
                }
            }
        ),

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
                    List<NumberOfSteps> numberOfStepsList = new ArrayList<>();

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
                            .setDistance(fitbitActivity.get("distance").asText(),
                                LengthUnitValue.LengthUnit.m.toString())
                            .setDuration(fitbitActivity.get("duration").asText(),
                                DurationUnitValue.DurationUnit.ms.toString())
                            .setStartTime(startTime).build();

                        NumberOfSteps numberOfSteps = new NumberOfStepsBuilder()
                            .setSteps(fitbitActivity.get("steps").asInt()).build();
                        numberOfSteps.setEffectiveTimeFrame(activity.getEffectiveTimeFrame());

                        activities.add(activity);
                        numberOfStepsList.add(numberOfSteps);
                    }

                    Collection<Object> results = new ArrayList<>();
                    results.addAll(activities);
                    results.addAll(numberOfStepsList);

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
        String dateString = "2014-07-13";
        String endPointUrl = DATA_URL;
        endPointUrl += "/1/user/-/"
            + fitbitDataType.getEndPoint() + "/date/" + dateString + ".json";

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
            if (shimDataRequest.getNormalize()) {
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
