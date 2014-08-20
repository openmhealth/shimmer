package org.openmhealth.shim.jawbone;

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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.build.ActivityBuilder;
import org.openmhealth.schema.pojos.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.build.NumberOfStepsBuilder;
import org.openmhealth.schema.pojos.build.SleepDurationBuilder;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Encapsulates parameters specific to jawbone api.
 */
public class JawboneShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "jawbone";

    private static final String DATA_URL = "https://jawbone.com/nudge/api/v.1.1/users/@me/";

    private static final String AUTHORIZE_URL = "https://jawbone.com/auth/oauth2/auth";

    private static final String TOKEN_URL = "https://jawbone.com/auth/oauth2/token";

    public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg";

    public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19";

    public static final ArrayList<String> JAWBONE_SCOPES =
        new ArrayList<String>(Arrays.asList("extended_read", "weight_read",
            "cardiac_read", "meal_read", "move_read", "sleep_read"));

    public JawboneShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                       AccessParametersRepo accessParametersRepo,
                       ShimServerConfig shimServerConfig1) {
        super(authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig1);
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getClientSecret() {
        return JAWBONE_CLIENT_SECRET;
    }

    @Override
    public String getClientId() {
        return JAWBONE_CLIENT_ID;
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
                    return ShimDataResponse.result(null);
                }
                ObjectMapper mapper = new ObjectMapper();
                for (Object rawWeight : jbWeights) {
                    JsonNode jbWeight = mapper.readTree(((JSONObject) rawWeight).toJSONString());
                    DateTime timeStamp = new DateTime(jbWeight.get("time_created").asLong() * 1000);

                    BodyWeight bodyWeight = new BodyWeightBuilder()
                        .setWeight(
                            jbWeight.get("weight").asText(),
                            MassUnitValue.MassUnit.kg.toString())
                        .setTimeTaken(timeStamp).build();

                    bodyWeights.add(bodyWeight);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(BodyWeight.SCHEMA_BODY_WEIGHT, bodyWeights);
                return ShimDataResponse.result(results);
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
                    return ShimDataResponse.result(null);
                }
                ObjectMapper mapper = new ObjectMapper();
                for (Object rawSleep : jbSleeps) {
                    JsonNode jbSleep = mapper.readTree(((JSONObject) rawSleep).toJSONString());
                    DateTime timeStamp = new DateTime(jbSleep.get("time_created").asLong() * 1000);

                    SleepDuration sleepDuration = new SleepDurationBuilder()
                        .setDuration(jbSleep.get("details").get("duration").asText(),
                            DurationUnitValue.DurationUnit.sec.toString())
                        .setDate(timeStamp).build();

                    sleepDurations.add(sleepDuration);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(SleepDuration.SCHEMA_SLEEP_DURATION, sleepDurations);
                return ShimDataResponse.result(results);
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
                    return ShimDataResponse.result(null);
                }
                ObjectMapper mapper = new ObjectMapper();
                for (Object rawWorkout : jbWorkouts) {
                    JsonNode jbWorkout = mapper.readTree(((JSONObject) rawWorkout).toJSONString());
                    DateTime timeStamp = new DateTime(jbWorkout.get("time_created").asLong() * 1000);
                    DateTime timeEnd = new DateTime(jbWorkout.get("time_completed").asLong() * 1000);

                    Activity activity = new ActivityBuilder()
                        .setActivityName(jbWorkout.get("title").asText())
                        .setDistance(jbWorkout.get("details").get("meters").asDouble() + "",
                            LengthUnitValue.LengthUnit.m.toString())
                        .setDuration(jbWorkout.get("details").get("time").asText(),
                            DurationUnitValue.DurationUnit.sec.toString())
                        .setStartTime(timeStamp)
                        .setEndTime(timeEnd).build();

                    activities.add(activity);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(Activity.SCHEMA_ACTIVITY, activities);
                return ShimDataResponse.result(results);
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

                List<NumberOfSteps> steps = new ArrayList<>();
                JsonPath stepsPath = JsonPath.compile("$.data.items[*].details.hourly_totals[*]");

                Object hourlyStepTotalsMap = JsonPath.read(rawJson, stepsPath.getPath());

                if (hourlyStepTotalsMap == null) {
                    return ShimDataResponse.empty();
                }

                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddhh");

                ObjectMapper mapper = new ObjectMapper();
                String jsonString = ((JSONArray) hourlyStepTotalsMap).toJSONString();
                ArrayNode nodes = (ArrayNode) mapper.readTree(jsonString);

                for (Object node1 : nodes) {
                    Map<String, JsonNode> jbSteps = mapper.convertValue(node1, HashMap.class);
                    for (String timestampStr : jbSteps.keySet()) {

                        DateTime dateTime = formatter.parseDateTime(timestampStr);
                        Map<String, Object> stepEntry = (Map<String, Object>) jbSteps.get(timestampStr);

                        steps.add(new NumberOfStepsBuilder()
                            .setStartTime(dateTime)
                            .setDuration(stepEntry.get("active_time").toString(),
                                DurationUnitValue.DurationUnit.sec.toString())
                            .setSteps((Integer) stepEntry.get("steps")).build());
                    }
                }
                Map<String, Object> results = new HashMap<>();
                results.put(NumberOfSteps.SCHEMA_NUMBER_OF_STEPS, steps);
                return ShimDataResponse.result(results);

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
                    ShimDataResponse.result(objectMapper.readTree(responseEntity.getBody())), HttpStatus.OK);
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
            + "&scope=" + StringUtils.collectionToDelimitedString(resource.getScope(), " ")
            + "&redirect_uri=" + getCallbackUrl();
        AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
        parameters.setRedirectUri(exception.getRedirectUri());
        parameters.setStateKey(exception.getStateKey());
        parameters.setHttpMethod(HttpMethod.GET);
        parameters.setAuthorizationUrl(authorizationUrl);
        return parameters;
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
            form.set("grant_type", resource.getGrantType());
        }
    }
}
