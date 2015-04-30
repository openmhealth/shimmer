package org.openmhealth.shim.misfit;

import static org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit.sec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.SleepDurationUnitValue;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.build.ActivityBuilder;
import org.openmhealth.schema.pojos.build.SleepDurationBuilder;
import org.openmhealth.schema.pojos.build.StepCountBuilder;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Joiner;

/**
 * Encapsulates parameters specific to the Misfit API.
 *
 * @author Eric Jain
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.misfit")
public class MisfitShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "misfit";

    private static final String DATA_URL = "https://api.misfitwearables.com/move/resource/v1/user/me";

    private static final String AUTHORIZE_URL = "https://api.misfitwearables.com/auth/dialog/authorize";

    private static final String TOKEN_URL = "https://api.misfitwearables.com/auth/tokens/exchange";

    public static final List<String> MISFIT_SCOPES = Arrays.asList("public", "birthday", "email");

    private static final Duration MAX_DURATION = Duration.standardDays(31);

    @Autowired
    public MisfitShim(ApplicationAccessParametersRepo applicationParametersRepo,
                       AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                       AccessParametersRepo accessParametersRepo,
                       ShimServerConfig shimServerConfig) {
        super(applicationParametersRepo, authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig);
    }

    @Override
    public String getLabel() {
        return "Misfit";
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
        return MISFIT_SCOPES;
    }

    @Override
    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new MisfitAuthorizationCodeAccessTokenProvider();
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return new MisfitDataTypes[] {
            MisfitDataTypes.SLEEP, MisfitDataTypes.ACTIVITIES, MisfitDataTypes.MOVES
        };
    }

    public enum MisfitDataTypes implements ShimDataType {

        SLEEP("/activity/sleeps", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("sleeps").size() == 0) {
                    return ShimDataResponse.empty(MisfitShim.SHIM_KEY);
                }
                List<SleepDuration> sleepDurations = new ArrayList<>();
                for (JsonNode node : responseNode.path("sleeps")) {
                    DateTime start = DateTime.parse(node.path("startTime").textValue());
                    SleepDuration sleepDuration = new SleepDurationBuilder()
                        .withStartAndDuration(start,
                            node.path("duration").doubleValue() / 60.0, 
                            SleepDurationUnitValue.Unit.min)
                        .build();

                    sleepDurations.add(sleepDuration);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(SleepDuration.SCHEMA_SLEEP_DURATION, sleepDurations);
                return ShimDataResponse.result(MisfitShim.SHIM_KEY, results);
            }
        }),

        ACTIVITIES("/activity/sessions", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("sessions").size() == 0) {
                    return ShimDataResponse.empty(MisfitShim.SHIM_KEY);
                }
                List<Activity> activities = new ArrayList<>();
                for (JsonNode node : responseNode.path("sessions")) {
                    DateTime start = DateTime.parse(node.path("startTime").textValue());
                    Activity activity = new ActivityBuilder()
                        .setActivityName(node.path("activityType").textValue())
                        .setDistance(node.path("distance").decimalValue(), LengthUnit.km)
                        .withStartAndDuration(start, node.path("duration").doubleValue(), sec)
                        .build();
                    activities.add(activity);
                }
                Map<String, Object> results = new HashMap<>();
                results.put(Activity.SCHEMA_ACTIVITY, activities);
                return ShimDataResponse.result(MisfitShim.SHIM_KEY, results);
            }
        }),

        MOVES("/activity/summary", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

                JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                if (responseNode.path("summary").size() == 0) {
                    return ShimDataResponse.empty(MisfitShim.SHIM_KEY);
                }
                List<StepCount> stepCounts = new ArrayList<>();
                for (JsonNode node : responseNode.path("summary")) {
                    if (node.path("steps").intValue() > 0) {
                        LocalDate date = LocalDate.parse(node.path("date").textValue());
                        stepCounts.add(new StepCountBuilder()
                            .withStartAndDuration(
                                date.toDateTimeAtStartOfDay(DateTimeZone.UTC),
                                1.0, DurationUnitValue.DurationUnit.d)
                            .setSteps(node.path("steps").asInt()).build());
                    }
                }
                Map<String, Object> results = new HashMap<>();
                results.put(StepCount.SCHEMA_STEP_COUNT, stepCounts);
                return ShimDataResponse.result(MisfitShim.SHIM_KEY, results);
            }
        });

        private String endPoint;

        private JsonDeserializer<ShimDataResponse> normalizer;

        MisfitDataTypes(String endPoint, JsonDeserializer<ShimDataResponse> normalizer) {
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
    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {
        String urlRequest = DATA_URL;

        final MisfitDataTypes misfitDataType;
        try {
            misfitDataType = MisfitDataTypes.valueOf(
                shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        urlRequest += misfitDataType.getEndPoint() + "?";

        DateTime today = new DateTime();
        DateTime startDate = shimDataRequest.getStartDate() == null ?
            today.minusDays(1) : shimDataRequest.getStartDate();
        DateTime endDate = shimDataRequest.getEndDate() == null ?
            today.plusDays(1) : shimDataRequest.getEndDate();
        if (new Duration(startDate, endDate).isLongerThan(MAX_DURATION)) {
            endDate = startDate.plus(MAX_DURATION).minusDays(1);
        }
        urlRequest += "&start_date=" + startDate.toLocalDate();
        urlRequest += "&end_date=" + endDate.toLocalDate();
        urlRequest += "&detail=true";

        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(urlRequest, byte[].class);
        try {
            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, misfitDataType.getNormalizer());
                objectMapper.registerModule(module);
                return new ResponseEntity<>(
                    objectMapper.readValue(responseEntity.getBody(), ShimDataResponse.class), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                    ShimDataResponse.result(MisfitShim.SHIM_KEY, objectMapper.readTree(responseEntity.getBody())), HttpStatus.OK);
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
            + "&scope=" + Joiner.on(',').join(resource.getScope())
            + "&redirect_uri=" + getCallbackUrl();
    }

    /**
     * Simple overrides to base spring class from oauth.
     */
    public class MisfitAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
        public MisfitAuthorizationCodeAccessTokenProvider() {
            this.setTokenRequestEnhancer(new MisfitTokenRequestEnhancer());
        }
    }

    /**
     * Adds jawbone required parameters to authorization token requests.
     */
    private class MisfitTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("redirect_uri", getCallbackUrl());
        }
    }
}
