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
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.build.ActivityBuilder;
import org.openmhealth.schema.pojos.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.build.NumberOfStepsBuilder;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.NumberOfSteps;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
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

    public FitbitShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo) {
        super(authorizationRequestParametersRepo);
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
                    JsonPath bodyWeightsPath = JsonPath.compile("$.things[*].data-xml.weight");

                    List<Object> hvWeights = JsonPath.read(rawJson, bodyWeightsPath.getPath());
                    if (CollectionUtils.isEmpty(hvWeights)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : hvWeights) {
                        JsonNode hvWeight = mapper.readTree(((JSONObject) fva).toJSONString());

                        DateTime dateTimeWhen = null; //parseDateTimeFromWhenNode(hvWeight.get("when"));

                        BodyWeight bodyWeight = new BodyWeightBuilder()
                            .setWeight(
                                hvWeight.get("value").get("display").get("").asText(),
                                MassUnitValue.MassUnit.lb.toString())
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
                    throws IOException, JsonProcessingException {
                    return ShimDataResponse.empty();
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
                    return ShimDataResponse.empty();
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
                    return ShimDataResponse.empty();
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

                    JsonPath activityPath = JsonPath.compile("$.activities[*]");

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
        String endPointUrl = DATA_URL;
        endPointUrl += "/1/user/-/"
            + fitbitDataType.getEndPoint() + "/date/2014-07-13.json";

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
            /*StringWriter writer = new StringWriter();
            IOUtils.copy(responseEntity.getContent(), writer);
            String jsonContent = "{date:}" writer.toString();*/

            ObjectMapper objectMapper = new ObjectMapper();
            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, fitbitDataType.getNormalizer());
                objectMapper.registerModule(module);
                return objectMapper.readValue(responseEntity.getContent(), ShimDataResponse.class);
            } else {
                return ShimDataResponse.result(objectMapper.readTree(responseEntity.getContent()));
            }
        } catch (IOException e) {
            throw new ShimException("Could not fetch data", e);
        }
    }
}
