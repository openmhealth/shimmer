/*
 * Copyright 2017 Open mHealth
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
 *
 */

package org.openmhealth.shim.misfit;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.openmhealth.shim.*;
import org.openmhealth.shim.misfit.mapper.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;


/**
 * @author Eric Jain
 * @author Emerson Farrugia
 */
@Component
public class MisfitShim extends OAuth2Shim {

    private static final Logger logger = getLogger(MisfitShim.class);

    public static final String SHIM_KEY = "misfit";
    private static final String DATA_URL = "https://api.misfitwearables.com/move/resource/v1/user/me";
    private static final String USER_AUTHORIZATION_URL = "https://api.misfitwearables.com/auth/dialog/authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.misfitwearables.com/auth/tokens/exchange";

    private static final long MAX_DURATION_IN_DAYS = 31;

    @Autowired
    private MisfitClientSettings clientSettings;

    private MisfitPhysicalActivityDataPointMapper physicalActivityMapper = new MisfitPhysicalActivityDataPointMapper();
    private MisfitSleepDurationDataPointMapper sleepDurationMapper = new MisfitSleepDurationDataPointMapper();
    private MisfitSleepEpisodeDataPointMapper sleepEpisodeMapper = new MisfitSleepEpisodeDataPointMapper();
    private MisfitStepCountDataPointMapper stepCountMapper = new MisfitStepCountDataPointMapper();

    @Override
    public String getLabel() {

        return "Misfit";
    }

    @Override
    public String getShimKey() {

        return SHIM_KEY;
    }

    @Override
    public String getUserAuthorizationUrl() {

        return USER_AUTHORIZATION_URL;
    }

    @Override
    public String getAccessTokenUrl() {

        return ACCESS_TOKEN_URL;
    }

    @Override
    protected OAuth2ClientSettings getClientSettings() {

        return clientSettings;
    }

    @Override
    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {

        return new MisfitAuthorizationCodeAccessTokenProvider();
    }

    @Override
    public ShimDataType[] getShimDataTypes() {

        return MisfitDataTypes.values();
    }


    // TODO remove this structure once endpoints are figured out
    public enum MisfitDataTypes implements ShimDataType {

        PHYSICAL_ACTIVITY("activity/sessions"),
        SLEEP_DURATION("activity/sleeps"),
        SLEEP_EPISODE("activity/sleeps"),
        STEP_COUNT("activity/summary");

        private String endPoint;

        MisfitDataTypes(String endPoint) {

            this.endPoint = endPoint;
        }

        public String getEndPoint() {

            return endPoint;
        }
    }

    @Override
    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest) throws ShimException {

        final MisfitDataTypes misfitDataType;
        try {
            misfitDataType = MisfitDataTypes.valueOf(shimDataRequest.getDataTypeKey().trim().toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: " + shimDataRequest.getDataTypeKey()
                    + " in shimDataRequest, cannot retrieve data.");
        }

        // TODO don't truncate dates
        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime startDateTime = shimDataRequest.getStartDateTime() == null ?
                now.minusDays(1) : shimDataRequest.getStartDateTime();

        OffsetDateTime endDateTime = shimDataRequest.getEndDateTime() == null ?
                now.plusDays(1) : shimDataRequest.getEndDateTime();

        if (Duration.between(startDateTime, endDateTime).toDays() > MAX_DURATION_IN_DAYS) {
            endDateTime =
                    startDateTime.plusDays(MAX_DURATION_IN_DAYS - 1);  // TODO when refactoring, break apart queries
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(DATA_URL);

        for (String pathSegment : Splitter.on("/").split(misfitDataType.getEndPoint())) {
            uriBuilder.pathSegment(pathSegment);
        }

        uriBuilder
                .queryParam("start_date", startDateTime.toLocalDate()) // TODO convert ODT to LocalDate properly
                .queryParam("end_date", endDateTime.toLocalDate())
                .queryParam("detail", true); // added to all endpoints to support summaries

        ResponseEntity<JsonNode> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(uriBuilder.build().encode().toUri(), JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // FIXME figure out how to handle this
            logger.error("A request for Misfit data failed.", e);
            throw e;
        }

        if (shimDataRequest.getNormalize()) {

            MisfitDataPointMapper<?> dataPointMapper;

            switch (misfitDataType) {
                case PHYSICAL_ACTIVITY:
                    dataPointMapper = physicalActivityMapper;
                    break;
                case SLEEP_DURATION:
                    dataPointMapper = sleepDurationMapper;
                    break;
                case SLEEP_EPISODE:
                    dataPointMapper = sleepEpisodeMapper;
                    break;
                case STEP_COUNT:
                    dataPointMapper = stepCountMapper;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            return ok().body(ShimDataResponse.result(SHIM_KEY,
                    dataPointMapper.asDataPoints(singletonList(responseEntity.getBody()))));
        }
        else {
            return ok().body(ShimDataResponse.result(SHIM_KEY, responseEntity.getBody()));
        }
    }

    @Override
    protected String getAuthorizationUrl(UserRedirectRequiredException exception, Map<String, String> addlParameters) {

        final OAuth2ProtectedResourceDetails resource = getResource();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(exception.getRedirectUri())
                .queryParam("state", exception.getStateKey())
                .queryParam("client_id", resource.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", Joiner.on(',').join(resource.getScope()))
                .queryParam("redirect_uri", getDefaultRedirectUrl());

        return uriBuilder.build().encode().toUriString();
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
        public void enhance(AccessTokenRequest request, OAuth2ProtectedResourceDetails resource,
                MultiValueMap<String, String> form, HttpHeaders headers) {

            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("redirect_uri", getDefaultRedirectUrl());
        }
    }
}
