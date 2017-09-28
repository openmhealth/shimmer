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

package org.openmhealth.shim.moves;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.shim.*;
import org.openmhealth.shim.moves.mapper.MovesPhysicalActivityDataPointMapper;
import org.openmhealth.shim.moves.mapper.MovesStepCountDataPointMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;


/**
 * @author Cheng-Kang Hsieh
 * @author Emerson Farrugia
 */
@Component
public class MovesShim extends OAuth2Shim {

    private static final Logger logger = LoggerFactory.getLogger(MovesShim.class);

    public static final String SHIM_KEY = "moves";
    private static final String DATA_URL = "https://api.moves-app.com/api/1.1";
    private static final String WEB_BASED_USER_AUTHORIZATION_URL = "https://api.moves-app.com/oauth/v1/authorize";
    private static final String APP_BASED_USER_AUTHORIZATION_URL = "moves://app/authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.moves-app.com/oauth/v1/access_token";

    @Autowired
    private MovesClientSettings clientSettings;

    private MovesPhysicalActivityDataPointMapper physicalActivityMapper = new MovesPhysicalActivityDataPointMapper();

    private MovesStepCountDataPointMapper stepCountMapper = new MovesStepCountDataPointMapper();

    @Override
    public String getLabel() {

        return "Moves";
    }

    @Override
    public String getShimKey() {

        return SHIM_KEY;
    }

    @Override
    public String getUserAuthorizationUrl() {

        return clientSettings.isAuthorizationInitiatedFromBrowser()
                ? WEB_BASED_USER_AUTHORIZATION_URL
                : APP_BASED_USER_AUTHORIZATION_URL;
    }

    @Override
    public String getAccessTokenUrl() {

        return ACCESS_TOKEN_URL;
    }

    @Override
    protected OAuth2ClientSettings getClientSettings() {

        return clientSettings;
    }

    public enum MovesDataType implements ShimDataType {

        PHYSICAL_ACTIVITY("/user/storyline/daily"),
        STEP_COUNT("/user/storyline/daily");

        private String endpoint;
        private int maximumRetrievalPeriodInDays = 31;

        MovesDataType(String endpoint) {

            this.endpoint = endpoint;
        }

        MovesDataType(String endpoint, int maximumRetrievalPeriodInDays) {

            this.endpoint = endpoint;
            this.maximumRetrievalPeriodInDays = maximumRetrievalPeriodInDays;
        }

        public String getEndPoint() {

            return endpoint;
        }

        public int getMaximumRetrievalPeriodInDays() {

            return maximumRetrievalPeriodInDays;
        }
    }


    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {

        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new MovesAccessTokenRequestEnhancer());
        return provider;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {

        return MovesDataType.values();
    }

    @Override
    protected ResponseEntity<ShimDataResponse> getData(
            OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest)
            throws ShimException {

        String dataTypeKey = shimDataRequest.getDataTypeKey().trim().toUpperCase();

        MovesDataType movesDataType;
        try {
            movesDataType = MovesDataType.valueOf(dataTypeKey);
        }
        catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                    + dataTypeKey + " in shimDataRequest, cannot retrieve data.");
        }

        LocalDate today = LocalDate.now();

        LocalDate startDate = shimDataRequest.getStartDateTime() == null
                ? today
                : shimDataRequest.getStartDateTime().toLocalDate();

        LocalDate endDate = shimDataRequest.getEndDateTime() == null
                ? today
                : shimDataRequest.getEndDateTime().toLocalDate();

        if (Period.between(startDate, endDate).getDays() > movesDataType.getMaximumRetrievalPeriodInDays()) {
            endDate = startDate.plusDays(movesDataType.getMaximumRetrievalPeriodInDays());
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(DATA_URL)
                .path(movesDataType.getEndPoint())
                .queryParam("from", startDate)
                .queryParam("to", endDate)
                .queryParam("trackPoints", false); // TODO make dynamic

        ResponseEntity<JsonNode> responseEntity;

        try {
            responseEntity = restTemplate.getForEntity(uriBuilder.build().encode().toUri(), JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // TODO figure out how to handle this
            logger.error("A request for Moves data failed.", e);
            throw e;
        }


        List<? extends DataPoint<?>> dataPoints;

        if (shimDataRequest.getNormalize()) {

            switch (movesDataType) {
                case PHYSICAL_ACTIVITY:
                    dataPoints = physicalActivityMapper.asDataPoints(responseEntity.getBody());
                    break;

                case STEP_COUNT:
                    dataPoints = stepCountMapper.asDataPoints(responseEntity.getBody());
                    break;

                default:
                    throw new UnsupportedOperationException();
            }

            return ok().body(ShimDataResponse.result(SHIM_KEY, dataPoints));
        }
        else {
            return ok().body(ShimDataResponse.result(SHIM_KEY, responseEntity.getBody()));
        }
    }

    @Override
    protected String getAuthorizationUrl(
            UserRedirectRequiredException exception,
            Map<String, String> additionalParameters) {

        final OAuth2ProtectedResourceDetails resource = getResource();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(exception.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", resource.getClientId())
                .queryParam("redirect_uri", getDefaultRedirectUrl())
                .queryParam("scope", Joiner.on(" ").join(resource.getScope()))
                .queryParam("state", exception.getStateKey());

        return uriBuilder.build().encode().toUriString();
    }

    private class MovesAccessTokenRequestEnhancer implements RequestEnhancer {

        @Override
        public void enhance(
                AccessTokenRequest request,
                OAuth2ProtectedResourceDetails resource,
                MultiValueMap<String, String> form,
                HttpHeaders headers) {

            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("redirect_uri", getDefaultRedirectUrl());
        }
    }
}
