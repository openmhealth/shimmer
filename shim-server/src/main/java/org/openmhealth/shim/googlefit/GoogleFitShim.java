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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.openmhealth.shim.*;
import org.openmhealth.shim.googlefit.mapper.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;


/**
 * Encapsulates parameters specific to the Google Fit API.
 *
 * @author Eric Jain
 * @author Chris Schaefbauer
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.googlefit")
public class GoogleFitShim extends OAuth2ShimBase {
    private static final Logger logger = getLogger(GoogleFitShim.class);

    public static final String SHIM_KEY = "googlefit";

    private static final String DATASET_URL =
            "https://www.googleapis.com/fitness/v1/users/me/dataSources/%s/datasets/%d-%d?limit=%d";

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
        return new GoogleFitDataTypes[] {
                GoogleFitDataTypes.ACTIVITY,
                GoogleFitDataTypes.BODY_HEIGHT,
                GoogleFitDataTypes.BODY_WEIGHT,
                GoogleFitDataTypes.HEART_RATE,
                GoogleFitDataTypes.STEP_COUNT,
                GoogleFitDataTypes.CALORIES_BURNED};
    }

    public enum GoogleFitDataTypes implements ShimDataType {

        ACTIVITY("derived:com.google.activity.segment:com.google.android.gms:merge_activity_segments"),
        BODY_HEIGHT("derived:com.google.height:com.google.android.gms:merge_height"),
        BODY_WEIGHT("derived:com.google.weight:com.google.android.gms:merge_weight"),
        HEART_RATE("derived:com.google.heart_rate.bpm:com.google.android.gms:merge_heart_rate_bpm"),
        STEP_COUNT("derived:com.google.step_count.delta:com.google.android.gms:merge_step_deltas"),
        CALORIES_BURNED("derived:com.google.calories.expended:com.google.android.gms:merge_calories_expended");

        private final String streamId;

        GoogleFitDataTypes(String streamId) {
            this.streamId = streamId;

        }

        public String getStreamId() {
            return streamId;
        }

    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest) throws ShimException {
        final GoogleFitDataTypes googleFitDataType;
        try {
            googleFitDataType = GoogleFitDataTypes.valueOf(
                    shimDataRequest.getDataTypeKey().trim().toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                    + shimDataRequest.getDataTypeKey()
                    + " in shimDataRequest, cannot retrieve data.");
        }

        long numToReturn = 100;
        if (shimDataRequest.getNumToReturn() != null) {
            numToReturn = shimDataRequest.getNumToReturn();
        }

        OffsetDateTime todayInUTC =
                LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);

        OffsetDateTime startDateInUTC = shimDataRequest.getStartDateTime() == null ?
                todayInUTC.minusDays(1) : shimDataRequest.getStartDateTime();
        long startTimeNanos = (startDateInUTC.toEpochSecond() * 1000000000) + startDateInUTC.toInstant().getNano();

        OffsetDateTime endDateInUTC = shimDataRequest.getEndDateTime() == null ?
                todayInUTC.plusDays(1) :
                shimDataRequest.getEndDateTime().plusDays(1);   // We are inclusive of the last day, so add 1 day to get
        // the end of day on the last day, which captures the
        // entire last day
        long endTimeNanos = (endDateInUTC.toEpochSecond() * 1000000000) + endDateInUTC.toInstant().getNano();

        String urlRequest =
                String.format(DATASET_URL, googleFitDataType.getStreamId(), startTimeNanos, endTimeNanos, numToReturn);

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseEntity<JsonNode> responseEntity;
        try{
            responseEntity = restTemplate.getForEntity(urlRequest, JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // TODO figure out how to handle this
            logger.error("A request for Google Fit data failed.", e);
            throw e;
        }

        if (shimDataRequest.getNormalize()) {
            GoogleFitDataPointMapper<?> dataPointMapper;
            switch(googleFitDataType){
                case BODY_WEIGHT:
                    dataPointMapper = new GoogleFitBodyWeightDataPointMapper();
                    break;
                case BODY_HEIGHT:
                    dataPointMapper = new GoogleFitBodyHeightDataPointMapper();
                    break;
                case ACTIVITY:
                    dataPointMapper = new GoogleFitPhysicalActivityDataPointMapper();
                    break;
                case STEP_COUNT:
                    dataPointMapper = new GoogleFitStepCountDataPointMapper();
                    break;
                case HEART_RATE:
                    dataPointMapper = new GoogleFitHeartRateDataPointMapper();
                    break;
                case CALORIES_BURNED:
                    dataPointMapper = new GoogleFitCaloriesBurnedDataPointMapper();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            SimpleModule module = new SimpleModule();
            module.addDeserializer(ShimDataResponse.class, googleFitDataType.getNormalizer());
            objectMapper.registerModule(module);

            return ok().body(ShimDataResponse.result(GoogleFitShim.SHIM_KEY,dataPointMapper.asDataPoints(
                    singletonList(responseEntity.getBody()))));
        }
        else {

            return ok().body(ShimDataResponse.result(GoogleFitShim.SHIM_KEY,responseEntity.getBody()));
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
