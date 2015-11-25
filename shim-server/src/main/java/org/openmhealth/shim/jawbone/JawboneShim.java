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

package org.openmhealth.shim.jawbone;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.shim.*;
import org.openmhealth.shim.jawbone.mapper.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Encapsulates parameters specific to the Jawbone API and processes requests for Jawbone data from shimmer.
 *
 * @author Danilo Bonilla
 * @author Chris Schaefbauer
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.jawbone")
public class JawboneShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "jawbone";

    private static final String DATA_URL = "https://jawbone.com/nudge/api/v.1.1/users/@me/";

    private static final String AUTHORIZE_URL = "https://jawbone.com/auth/oauth2/auth";

    private static final String TOKEN_URL = "https://jawbone.com/auth/oauth2/token";

    public static final List<String> JAWBONE_SCOPES = Arrays.asList(
            "extended_read", "weight_read", "heartrate_read", "meal_read", "move_read", "sleep_read");

    private static final Logger logger = getLogger(JawboneShim.class);

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
    public ShimDataType[] getShimDataTypes() {

        return new JawboneDataTypes[] {
                JawboneDataTypes.SLEEP, JawboneDataTypes.ACTIVITY, JawboneDataTypes.BODY_MASS_INDEX,
                JawboneDataTypes.WEIGHT, JawboneDataTypes.HEART_RATE, JawboneDataTypes.STEPS};
    }

    public enum JawboneDataTypes implements ShimDataType {

        SLEEP("sleeps"),
        ACTIVITY("workouts"),
        WEIGHT("body_events"),
        STEPS("moves"),
        BODY_MASS_INDEX("body_events"),
        HEART_RATE("heartrates");

        private String endPoint;

        JawboneDataTypes(String endPoint) {

            this.endPoint = endPoint;
        }

        public String getEndPoint() {

            return endPoint;
        }
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest) throws ShimException {

        final JawboneDataTypes jawboneDataType;
        try {
            jawboneDataType = JawboneDataTypes.valueOf(
                    shimDataRequest.getDataTypeKey().trim().toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                    + shimDataRequest.getDataTypeKey()
                    + " in shimDataRequest, cannot retrieve data.");
        }

        // FIXME this needs to get changed or documented
        long numToReturn = 100;
        if (shimDataRequest.getNumToReturn() != null) {
            numToReturn = shimDataRequest.getNumToReturn();
        }

        OffsetDateTime today = OffsetDateTime.now();

        OffsetDateTime startDateTime = shimDataRequest.getStartDateTime() == null ?
                today.minusDays(1) : shimDataRequest.getStartDateTime();
        long startTimeInEpochSecond = startDateTime.toEpochSecond();

        // We are inclusive of the last day, so we need to add an extra day since we are dealing with start of day,
        // and would miss the activities that occurred during the last day within going to midnight of that day
        OffsetDateTime endDateTime = shimDataRequest.getEndDateTime() == null ?
                today.plusDays(1) : shimDataRequest.getEndDateTime().plusDays(1);
        long endTimeInEpochSecond = endDateTime.toEpochSecond();

        UriComponentsBuilder uriComponentsBuilder =
                UriComponentsBuilder.fromUriString(DATA_URL).path(jawboneDataType.getEndPoint())
                        .queryParam("start_time", startTimeInEpochSecond).queryParam("end_time", endTimeInEpochSecond)
                        .queryParam("limit", numToReturn);

        ResponseEntity<JsonNode> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(uriComponentsBuilder.build().encode().toUri(), JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // FIXME figure out how to handle this
            logger.error("A request for Jawbone data failed.", e);
            throw e;
        }

        if (shimDataRequest.getNormalize()) {

            JawboneDataPointMapper mapper;
            switch ( jawboneDataType ) {
                case WEIGHT:
                    mapper = new JawboneBodyWeightDataPointMapper();
                    break;
                case STEPS:
                    mapper = new JawboneStepCountDataPointMapper();
                    break;
                case BODY_MASS_INDEX:
                    mapper = new JawboneBodyMassIndexDataPointMapper();
                    break;
                case ACTIVITY:
                    mapper = new JawbonePhysicalActivityDataPointMapper();
                    break;
                case SLEEP:
                    mapper = new JawboneSleepDurationDataPointMapper();
                    break;
                case HEART_RATE:
                    mapper = new JawboneHeartRateDataPointMapper();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            return ResponseEntity.ok().body(ShimDataResponse
                    .result(JawboneShim.SHIM_KEY, mapper.asDataPoints(singletonList(responseEntity.getBody()))));

        }
        else {

            return ResponseEntity.ok().body(ShimDataResponse.result(JawboneShim.SHIM_KEY, responseEntity.getBody()));
        }

    }

    @Override
    protected String getAuthorizationUrl(UserRedirectRequiredException exception) {

        final OAuth2ProtectedResourceDetails resource = getResource();
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(exception.getRedirectUri())
                .queryParam("state", exception.getStateKey())
                .queryParam("client_id", resource.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", StringUtils.collectionToDelimitedString(resource.getScope(), " "))
                .queryParam("redirect_uri", getCallbackUrl());

        return uriBuilder.build().encode().toUriString();

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
