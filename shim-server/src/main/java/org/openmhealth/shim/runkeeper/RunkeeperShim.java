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

package org.openmhealth.shim.runkeeper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.shim.*;
import org.openmhealth.shim.runkeeper.mapper.RunkeeperCaloriesBurnedDataPointMapper;
import org.openmhealth.shim.runkeeper.mapper.RunkeeperDataPointMapper;
import org.openmhealth.shim.runkeeper.mapper.RunkeeperPhysicalActivityDataPointMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
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

import java.time.OffsetDateTime;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.ResponseEntity.ok;


/**
 * @author Danilo Bonilla
 * @author Emerson Farrugia
 * @author Chris Schaefbauer
 */
@Component
public class RunkeeperShim extends OAuth2Shim {

    private static final Logger logger = getLogger(RunkeeperShim.class);

    public static final String SHIM_KEY = "runkeeper";
    private static final String DATA_URL = "https://api.runkeeper.com";
    private static final String USER_AUTHORIZATION_URL = "https://runkeeper.com/apps/authorize";
    private static final String ACCESS_TOKEN_URL = "https://runkeeper.com/apps/token";

    @Autowired
    private RunkeeperClientSettings clientSettings;

    @Override
    public String getLabel() {

        return "Runkeeper";
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

        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new RunkeeperTokenRequestEnhancer());
        return provider;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {

        return RunkeeperDataType.values();
    }


    // TODO remove this structure once endpoints are figured out
    public enum RunkeeperDataType implements ShimDataType {

        CALORIES_BURNED("application/vnd.com.runkeeper.FitnessActivityFeed+json", "fitnessActivities"),
        PHYSICAL_ACTIVITY("application/vnd.com.runkeeper.FitnessActivityFeed+json", "fitnessActivities");

        private String dataTypeHeader;
        private String endPointUrl;

        RunkeeperDataType(String dataTypeHeader, String endPointUrl) {

            this.dataTypeHeader = dataTypeHeader;
            this.endPointUrl = endPointUrl;
        }

        public String getDataTypeHeader() {

            return dataTypeHeader;
        }

        public String getEndPointUrl() {

            return endPointUrl;
        }
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest) throws ShimException {

        String dataTypeKey = shimDataRequest.getDataTypeKey().trim().toUpperCase();

        RunkeeperDataType runkeeperDataType;
        try {
            runkeeperDataType = RunkeeperDataType.valueOf(dataTypeKey);
        }
        catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: " + dataTypeKey +
                    " in shimDataRequest, cannot retrieve data.");
        }

        /***
         * Setup default date parameters
         */
        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime startDateTime = shimDataRequest.getStartDateTime() == null ?
                now.minusDays(1) : shimDataRequest.getStartDateTime();

        OffsetDateTime endDateTime = shimDataRequest.getEndDateTime() == null ?
                now.plusDays(1) : shimDataRequest.getEndDateTime();

        /*
            Runkeeper defaults to returning a maximum of 25 entries per request (pageSize = 25 by default), so
            we override the default by specifying an arbitrarily large number as the pageSize.
         */
        long numToReturn = 100_000;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(DATA_URL)
                .pathSegment(runkeeperDataType.getEndPointUrl())
                .queryParam("noEarlierThan", startDateTime.toLocalDate())
                .queryParam("noLaterThan", endDateTime.toLocalDate())
                .queryParam("pageSize", numToReturn)
                .queryParam("detail", true); // added to all endpoints to support summaries


        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", runkeeperDataType.getDataTypeHeader());

        ResponseEntity<JsonNode> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uriBuilder.build().encode().toUri(), GET,
                    new HttpEntity<JsonNode>(headers), JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // FIXME figure out how to handle this
            logger.error("A request for RunKeeper data failed.", e);
            throw e;
        }

        if (shimDataRequest.getNormalize()) {
            RunkeeperDataPointMapper<?> dataPointMapper;
            switch (runkeeperDataType) {
                case CALORIES_BURNED:
                    dataPointMapper = new RunkeeperCaloriesBurnedDataPointMapper();
                    break;
                case PHYSICAL_ACTIVITY:
                    dataPointMapper = new RunkeeperPhysicalActivityDataPointMapper();
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
                .queryParam("redirect_uri", getDefaultRedirectUrl());

        return uriBuilder.build().encode().toUriString();
    }

    private class RunkeeperTokenRequestEnhancer implements RequestEnhancer {

        @Override
        public void enhance(AccessTokenRequest request,
                OAuth2ProtectedResourceDetails resource,
                MultiValueMap<String, String> form, HttpHeaders headers) {

            // TODO code?
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("grant_type", resource.getGrantType());
            form.set("redirect_uri", getDefaultRedirectUrl());
        }
    }
}
