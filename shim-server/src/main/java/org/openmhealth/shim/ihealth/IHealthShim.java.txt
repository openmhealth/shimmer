package org.openmhealth.shim.ihealth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BloodGlucose;
import org.openmhealth.schema.pojos.BloodPressure;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.DataPoint;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.build.ActivityBuilder;
import org.openmhealth.schema.pojos.build.BloodGlucoseBuilder;
import org.openmhealth.schema.pojos.build.BloodPressureBuilder;
import org.openmhealth.schema.pojos.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.build.HeartRateBuilder;
import org.openmhealth.schema.pojos.build.SleepDurationBuilder;
import org.openmhealth.schema.pojos.build.StepCountBuilder;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.shim.AccessParametersRepo;
import org.openmhealth.shim.ApplicationAccessParametersRepo;
import org.openmhealth.shim.AuthorizationRequestParametersRepo;
import org.openmhealth.shim.OAuth2ShimBase;
import org.openmhealth.shim.ShimDataRequest;
import org.openmhealth.shim.ShimDataResponse;
import org.openmhealth.shim.ShimDataType;
import org.openmhealth.shim.ShimException;
import org.openmhealth.shim.ShimServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseExtractor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

@Component
@ConfigurationProperties(prefix = "openmhealth.shim.ihealth")
public class IHealthShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "ihealth";

    private static final String API_URL = "https://api.ihealthlabs.com:8443/openapiv2";

    private static final String AUTHORIZE_URL = "https://api.ihealthlabs.com:8443/OpenApiV2/OAuthv2/userauthorization/";

    private static final String TOKEN_URL = AUTHORIZE_URL;

    public static final List<String> IHEALTH_SCOPES = Arrays.asList("OpenApiActivity", "OpenApiBP", "OpenApiSleep", 
        "OpenApiWeight", "OpenApiBG", "OpenApiSpO2", "OpenApiUserInfo", "OpenApiFood", "OpenApiSport");

    @Autowired
    public IHealthShim(ApplicationAccessParametersRepo applicationParametersRepo,
                       AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                       AccessParametersRepo accessParametersRepo,
                       ShimServerConfig shimServerConfig) {
        super(applicationParametersRepo, authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig);
    }

    @Override
    public String getLabel() {
        return "iHealth";
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
        return IHEALTH_SCOPES;
    }

    @Override
    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new IHealthAuthorizationCodeAccessTokenProvider();
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return new ShimDataType[] {
            IHealthDataTypes.ACTIVITY,
            IHealthDataTypes.BLOOD_GLUCOSE,
            IHealthDataTypes.BLOOD_PRESSURE,
            IHealthDataTypes.BODY_WEIGHT,
            IHealthDataTypes.SLEEP,
            IHealthDataTypes.STEP_COUNT
        };
    }

    public enum IHealthDataTypes implements ShimDataType {

        ACTIVITY("sport", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("SPORTDataList").size() == 0) {
                    return ShimDataResponse.empty(IHealthShim.SHIM_KEY);
                }
                List<Activity> activities = new ArrayList<>();
                for (JsonNode node : responseNode.path("SPORTDataList")) {
                    activities.add(new ActivityBuilder()
                        .withStartAndEnd(dateTimeValue(node.path("SportStartTime")), dateTimeValue(node.path("SportEndTime")))
                        .setActivityName(textValue(node.path("SportName"))).build());
                }
                Map<String, Object> results = new HashMap<>();
                results.put(Activity.SCHEMA_ACTIVITY, activities);
                return ShimDataResponse.result(IHealthShim.SHIM_KEY, results);
            }
        }),

        BLOOD_GLUCOSE("glucose", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("BGDataList").size() == 0) {
                    return ShimDataResponse.empty(IHealthShim.SHIM_KEY);
                }
                List<DataPoint> datapoints = new ArrayList<>();
                for (JsonNode node : responseNode.path("BGDataList")) {
                    datapoints.add(new BloodGlucoseBuilder().setTimeTaken(dateTimeValue(node.path("MDate")))
                        .setMgdLValue(node.path("BG").decimalValue())
                        .setNotes(textValue(node.path("Note"))).build());
                }
                Map<String, Object> results = new HashMap<>();
                results.put(BloodGlucose.SCHEMA_BLOOD_GLUCOSE, datapoints);
                return ShimDataResponse.result(IHealthShim.SHIM_KEY, results);
            }
        }),

        BLOOD_PRESSURE("bp", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("BPDataList").size() == 0) {
                    return ShimDataResponse.empty(IHealthShim.SHIM_KEY);
                }
                List<DataPoint> bloodPressure = new ArrayList<>();
                List<DataPoint> heartRate = new ArrayList<>();
                for (JsonNode node : responseNode.path("BPDataList")) {
                    DateTime start = dateTimeValue(node.path("MDate"));
                    bloodPressure.add(new BloodPressureBuilder().setTimeTaken(start)
                        .setValues(node.path("HP").decimalValue(), node.path("LP").decimalValue())
                        .setNotes(textValue(node.path("Note"))).build());
                    heartRate.add(new HeartRateBuilder().withTimeTaken(start)
                        .withRate(node.path("HR").intValue()).build());
                }
                Map<String, Object> results = new HashMap<>();
                results.put(BloodPressure.SCHEMA_BLOOD_PRESSURE, bloodPressure);
                results.put(HeartRate.SCHEMA_HEART_RATE, heartRate);
                return ShimDataResponse.result(IHealthShim.SHIM_KEY, results);
            }
        }),

        BODY_WEIGHT("weight", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("WeightDataList").size() == 0) {
                    return ShimDataResponse.empty(IHealthShim.SHIM_KEY);
                }
                List<DataPoint> datapoints = new ArrayList<>();
                for (JsonNode node : responseNode.path("WeightDataList")) {
                    datapoints.add(new BodyWeightBuilder().setTimeTaken(dateTimeValue(node.path("MDate")))
                        .setWeight(node.path("WeightValue").asText(), "kg").build());
                }
                Map<String, Object> results = new HashMap<>();
                results.put(BodyWeight.SCHEMA_BODY_WEIGHT, datapoints);
                return ShimDataResponse.result(IHealthShim.SHIM_KEY, results);
            }
        }),

        SLEEP("sleep", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("SRDataList").size() == 0) {
                    return ShimDataResponse.empty(IHealthShim.SHIM_KEY);
                }
                List<SleepDuration> sleepDurations = new ArrayList<>();
                for (JsonNode node : responseNode.path("SRDataList")) {
                    DateTime start = dateTimeValue(node.path("StartTime"));
                    DateTime end = dateTimeValue(node.path("EndTime"));
                    sleepDurations.add(new SleepDurationBuilder().withStartAndEnd(start, end)
                        .setNotes(textValue(node.path("Note"))).build());
                }
                Map<String, Object> results = new HashMap<>();
                results.put(SleepDuration.SCHEMA_SLEEP_DURATION, sleepDurations);
                return ShimDataResponse.result(IHealthShim.SHIM_KEY, results);
            }
        }),

        STEP_COUNT("activity", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("ARDataList").size() == 0) {
                    return ShimDataResponse.empty(IHealthShim.SHIM_KEY);
                }
                List<StepCount> stepCounts = new ArrayList<>();
                for (JsonNode node : responseNode.path("ARDataList")) {
                    if (node.path("Steps").intValue() > 0) {
                        DateTime start = dateTimeValue(node.path("MDate"));
                        stepCounts.add(new StepCountBuilder()
                            .withStartAndDuration(start, 1.0, DurationUnitValue.DurationUnit.d)
                            .setSteps(node.path("Steps").intValue()).build());
                    }
                }
                Map<String, Object> results = new HashMap<>();
                results.put(StepCount.SCHEMA_STEP_COUNT, stepCounts);
                return ShimDataResponse.result(IHealthShim.SHIM_KEY, results);
            }
        });

        private String endPoint;

        private JsonDeserializer<ShimDataResponse> normalizer;

        IHealthDataTypes(String endPoint, JsonDeserializer<ShimDataResponse> normalizer) {
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

        private static DateTime dateTimeValue(JsonNode node) {
            Preconditions.checkArgument(node.isIntegralNumber());
            return new DateTime(node.longValue() * 1000, DateTimeZone.UTC);
        }

        private static String textValue(JsonNode node) {
            Preconditions.checkArgument(node.isTextual());
            return Strings.emptyToNull(node.textValue().trim());
        }
    }

    @Override
    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {

        final IHealthDataTypes dataType;
        try {
            dataType = IHealthDataTypes.valueOf(
                shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        OAuth2AccessToken token = SerializationUtils.deserialize(shimDataRequest.getAccessParameters().getSerializedToken());
        String userId = Preconditions.checkNotNull((String) token.getAdditionalInformation().get("UserID"));
        String urlRequest = API_URL + "/user/" + userId + "/" +  dataType.getEndPoint() + ".json?";
        DateTime now = new DateTime();
        DateTime startDate = shimDataRequest.getStartDate() == null ?
            now.minusDays(1) : shimDataRequest.getStartDate();
        DateTime endDate = shimDataRequest.getEndDate() == null ?
            now.plusDays(1) : shimDataRequest.getEndDate();
        urlRequest += "&start_time=" + startDate.getMillis() / 1000;
        urlRequest += "&end_time=" + endDate.getMillis() / 1000;
        urlRequest += "&page_index=1";
        urlRequest += "&client_id=" + restTemplate.getResource().getClientId();
        urlRequest += "&client_secret=" + restTemplate.getResource().getClientSecret();
        urlRequest += "&access_token=" + token.getValue();
        urlRequest += "&locale=default";

        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(urlRequest, byte[].class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, dataType.getNormalizer());
                objectMapper.registerModule(module);
                return new ResponseEntity<>(
                    objectMapper.readValue(responseEntity.getBody(), ShimDataResponse.class), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                    ShimDataResponse.result(IHealthShim.SHIM_KEY, objectMapper.readTree(responseEntity.getBody())), HttpStatus.OK);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not read response data.");
        }
    }

    @Override
    public OAuth2ProtectedResourceDetails getResource() {
        AuthorizationCodeResourceDetails resource = (AuthorizationCodeResourceDetails) super.getResource();
        resource.setAuthenticationScheme(AuthenticationScheme.none);
        return resource;
    }

    @Override
    protected String getAuthorizationUrl(UserRedirectRequiredException exception) {
        final OAuth2ProtectedResourceDetails resource = getResource();
        return exception.getRedirectUri()
            + "?client_id=" + resource.getClientId()
            + "&response_type=code"
            + "&APIName=" + Joiner.on(' ').join(resource.getScope())
            + "&redirect_uri=" + getCallbackUrl() + "?state=" + exception.getStateKey();
    }

    public class IHealthAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
        
        public IHealthAuthorizationCodeAccessTokenProvider() {
            this.setTokenRequestEnhancer(new RequestEnhancer() {
                
                @Override
                public void enhance(AccessTokenRequest request,
                                    OAuth2ProtectedResourceDetails resource,
                                    MultiValueMap<String, String> form, HttpHeaders headers) {
                    
                    form.set("client_id", resource.getClientId());
                    form.set("client_secret", resource.getClientSecret());
                    form.set("redirect_uri", getCallbackUrl());
                    form.set("state", request.getStateKey());
                }
            });
        }

        @Override
        protected HttpMethod getHttpMethod() {
            return HttpMethod.GET;
        }

        @Override
        protected ResponseExtractor<OAuth2AccessToken> getResponseExtractor() {
            return new ResponseExtractor<OAuth2AccessToken>() {

                @Override
                public OAuth2AccessToken extractData(ClientHttpResponse response) throws IOException {

                    JsonNode node = new ObjectMapper().readTree(response.getBody());
                    String token = Preconditions.checkNotNull(node.path("AccessToken").textValue(), "Missing access token: %s", node);
                    String refreshToken = Preconditions.checkNotNull(node.path("RefreshToken").textValue(), "Missing refresh token: %s" + node);
                    String userId = Preconditions.checkNotNull(node.path("UserID").textValue(), "Missing UserID: %s", node);
                    long expiresIn = node.path("Expires").longValue() * 1000;
                    Preconditions.checkArgument(expiresIn > 0, "Missing Expires: %s", node);

                    DefaultOAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(token);
                    accessToken.setExpiration(new Date(System.currentTimeMillis() + expiresIn));
                    accessToken.setRefreshToken(new DefaultOAuth2RefreshToken(refreshToken));
                    accessToken.setAdditionalInformation(ImmutableMap.<String, Object>of("UserID", userId));
                    return accessToken;
                }
            };
        }
    }
}
