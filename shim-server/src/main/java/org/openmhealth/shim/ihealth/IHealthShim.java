/*
 * Copyright 2015 Open mHealth
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

package org.openmhealth.shim.ihealth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.openmhealth.shim.*;
import org.openmhealth.shim.ihealth.mapper.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.openmhealth.shim.ihealth.IHealthShim.IHealthDataTypes.*;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Encapsulates parameters specific to the iHealth REST API and processes requests made of shimmer for iHealth data.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "openmhealth.shim.ihealth")
public class IHealthShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "ihealth";

    private static final String API_URL = "https://api.ihealthlabs.com:8443/openapiv2/";

    private static final String AUTHORIZE_URL = "https://api.ihealthlabs.com:8443/OpenApiV2/OAuthv2/userauthorization/";

    private static final String TOKEN_URL = AUTHORIZE_URL;

    public static final List<String> IHEALTH_SCOPES = Arrays.asList("OpenApiActivity", "OpenApiBP", "OpenApiSleep",
            "OpenApiWeight", "OpenApiBG", "OpenApiSpO2", "OpenApiUserInfo", "OpenApiFood", "OpenApiSport");

    private static final Logger logger = getLogger(IHealthShim.class);

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
                PHYSICAL_ACTIVITY,
                BLOOD_GLUCOSE,
                BLOOD_PRESSURE,
                BODY_WEIGHT,
                BODY_MASS_INDEX,
                HEART_RATE,
                STEP_COUNT,
                SLEEP_DURATION
        };
    }

    /**
     * Map of values auto-configured from the application.yaml.
     */
    Map<String, String> serialValues;

    public Map<String, String> getSerialValues() {

        return serialValues;
    }

    public void setSerialValues(Map<String, String> serialValues) {

        this.serialValues = serialValues;
    }

    public enum IHealthDataTypes implements ShimDataType {

        PHYSICAL_ACTIVITY(singletonList("sport.json")),
        BLOOD_GLUCOSE(singletonList("glucose.json")),
        BLOOD_PRESSURE(singletonList("bp.json")),
        BODY_WEIGHT(singletonList("weight.json")),
        BODY_MASS_INDEX(singletonList("weight.json")),
        HEART_RATE(newArrayList("bp.json", "spo2.json")),
        STEP_COUNT(singletonList("activity.json")),
        SLEEP_DURATION(singletonList("sleep.json"));

        private List<String> endPoint;

        IHealthDataTypes(List<String> endPoint) {

            this.endPoint = endPoint;
        }

        public List<String> getEndPoint() {
            return endPoint;
        }

    }

    @Override
    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest) throws ShimException {

        final IHealthDataTypes dataType;
        try {
            dataType = valueOf(
                    shimDataRequest.getDataTypeKey().trim().toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                    + shimDataRequest.getDataTypeKey()
                    + " in shimDataRequest, cannot retrieve data.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startDate = shimDataRequest.getStartDateTime() == null ?
                now.minusDays(1) : shimDataRequest.getStartDateTime();
        OffsetDateTime endDate = shimDataRequest.getEndDateTime() == null ?
                now.plusDays(1) : shimDataRequest.getEndDateTime();

        /*
            The physical activity point handles start and end datetimes differently than the other endpoints. It
            requires use to include the range until the beginning of the next day.
         */
        if (dataType == PHYSICAL_ACTIVITY) {

            endDate = endDate.plusDays(1);
        }

        // SC and SV values are client-based keys that are unique to each endpoint within a project
        String scValue = getScValue();
        List<String> svValues = getSvValues(dataType);

        List<JsonNode> responseEntities = newArrayList();

        int i = 0;

        // We iterate because one of the measures (Heart rate) comes from multiple endpoints, so we submit
        // requests to each of these endpoints, map the responses separately and then combine them
        for (String endPoint : dataType.getEndPoint()) {

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(API_URL);

            // Need to use a dummy userId if we haven't authenticated yet. This is the case where we are using
            // getData to trigger Spring to conduct the OAuth exchange
            String userId = "uk";

            if (shimDataRequest.getAccessParameters() != null) {

                OAuth2AccessToken token =
                        SerializationUtils.deserialize(shimDataRequest.getAccessParameters().getSerializedToken());

                userId = Preconditions.checkNotNull((String) token.getAdditionalInformation().get("UserID"));
                uriBuilder.queryParam("access_token", token.getValue());
            }

            uriBuilder.path("/user/")
                    .path(userId + "/")
                    .path(endPoint)
                    .queryParam("client_id", restTemplate.getResource().getClientId())
                    .queryParam("client_secret", restTemplate.getResource().getClientSecret())
                    .queryParam("start_time", startDate.toEpochSecond())
                    .queryParam("end_time", endDate.toEpochSecond())
                    .queryParam("locale", "default")
                    .queryParam("sc", scValue)
                    .queryParam("sv", svValues.get(i));

            ResponseEntity<JsonNode> responseEntity;

            try {
                URI url = uriBuilder.build().encode().toUri();
                responseEntity = restTemplate.getForEntity(url, JsonNode.class);
            }
            catch (HttpClientErrorException | HttpServerErrorException e) {
                // FIXME figure out how to handle this
                logger.error("A request for iHealth data failed.", e);
                throw e;
            }

            if (shimDataRequest.getNormalize()) {

                IHealthDataPointMapper mapper;

                switch ( dataType ) {

                    case PHYSICAL_ACTIVITY:
                        mapper = new IHealthPhysicalActivityDataPointMapper();
                        break;
                    case BLOOD_GLUCOSE:
                        mapper = new IHealthBloodGlucoseDataPointMapper();
                        break;
                    case BLOOD_PRESSURE:
                        mapper = new IHealthBloodPressureDataPointMapper();
                        break;
                    case BODY_WEIGHT:
                        mapper = new IHealthBodyWeightDataPointMapper();
                        break;
                    case BODY_MASS_INDEX:
                        mapper = new IHealthBodyMassIndexDataPointMapper();
                        break;
                    case STEP_COUNT:
                        mapper = new IHealthStepCountDataPointMapper();
                        break;
                    case SLEEP_DURATION:
                        mapper = new IHealthSleepDurationDataPointMapper();
                        break;
                    case HEART_RATE:
                        // there are two different mappers for heart rate because the data can come from two endpoints
                        if (endPoint == "bp.json") {
                            mapper = new IHealthBloodPressureEndpointHeartRateDataPointMapper();
                            break;
                        }
                        else if (endPoint == "spo2.json") {
                            mapper = new IHealthBloodOxygenEndpointHeartRateDataPointMapper();
                            break;
                        }
                    default:
                        throw new UnsupportedOperationException();
                }

                responseEntities.addAll(mapper.asDataPoints(singletonList(responseEntity.getBody())));

            }
            else {
                responseEntities.add(responseEntity.getBody());
            }

            i++;

        }

        return ResponseEntity.ok().body(
                ShimDataResponse.result(SHIM_KEY, responseEntities));
    }

    private String getScValue() {

        return serialValues.get("SC");
    }

    private List<String> getSvValues(IHealthDataTypes dataType) {

        switch ( dataType ) {
            case PHYSICAL_ACTIVITY:
                return singletonList(serialValues.get("sportSV"));
            case BODY_WEIGHT:
                return singletonList(serialValues.get("weightSV"));
            case BODY_MASS_INDEX:
                return singletonList(serialValues.get("weightSV")); // body mass index comes from the weight endpoint
            case BLOOD_PRESSURE:
                return singletonList(serialValues.get("bloodPressureSV"));
            case BLOOD_GLUCOSE:
                return singletonList(serialValues.get("bloodGlucoseSV"));
            case STEP_COUNT:
                return singletonList(serialValues.get("activitySV"));
            case SLEEP_DURATION:
                return singletonList(serialValues.get("sleepSV"));
            case HEART_RATE:
                return newArrayList(serialValues.get("bloodPressureSV"), serialValues.get("spo2SV"));
            default:
                throw new UnsupportedOperationException();
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

        UriComponentsBuilder callBackUriBuilder = UriComponentsBuilder.fromUriString(getCallbackUrl())
                .queryParam("state", exception.getStateKey());

        UriComponentsBuilder authorizationUriBuilder = UriComponentsBuilder.fromUriString(exception.getRedirectUri())
                .queryParam("client_id", resource.getClientId())
                .queryParam("response_type", "code")
                .queryParam("APIName", Joiner.on(' ').join(resource.getScope()))
                .queryParam("redirect_uri", callBackUriBuilder.build().toString());

        return authorizationUriBuilder.build().encode().toString();
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
                    String token = Preconditions
                            .checkNotNull(node.path("AccessToken").textValue(), "Missing access token: %s", node);
                    String refreshToken = Preconditions
                            .checkNotNull(node.path("RefreshToken").textValue(), "Missing refresh token: %s" + node);
                    String userId =
                            Preconditions.checkNotNull(node.path("UserID").textValue(), "Missing UserID: %s", node);
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
