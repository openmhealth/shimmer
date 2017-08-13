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

package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.shim.*;
import org.openmhealth.shim.fitbit.mapper.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.openmhealth.shim.ShimDataResponse.result;
import static org.openmhealth.shim.fitbit.FitbitShim.FitbitDataType.HEART_RATE;
import static org.openmhealth.shim.fitbit.FitbitShim.FitbitDataType.STEP_COUNT;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;


/**
 * @author Danilo Bonilla
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
@Component
public class FitbitShim extends OAuth2Shim {

    public static final String SHIM_KEY = "fitbit";
    private static final String DATA_URL = "https://api.fitbit.com";
    private static final String USER_AUTHORIZATION_URL = "https://www.fitbit.com/oauth2/authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.fitbit.com/oauth2/token";

    private static final Logger logger = getLogger(FitbitShim.class);

    @Autowired
    private FitbitClientSettings fitbitClientSettings;

    @Autowired
    private FitbitAuthorizationCodeAccessTokenProvider fitbitAuthorizationCodeAccessTokenProvider;

    @Override
    public String getLabel() {

        return "Fitbit";
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

        return fitbitClientSettings;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {

        return FitbitDataType.values();
    }

    @Override
    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {

        return fitbitAuthorizationCodeAccessTokenProvider;
    }

    public enum FitbitDataType implements ShimDataType {

        BODY_MASS_INDEX("body/log/weight"),
        BODY_WEIGHT("body/log/weight"),
        HEART_RATE("activities/heart"),
        PHYSICAL_ACTIVITY("activities"),
        SLEEP_DURATION("sleep", "1.2"),
        SLEEP_EPISODE("sleep", "1.2"),
        STEP_COUNT("activities/steps");

        private String endpoint;
        private String version = "1";

        FitbitDataType(String endpoint) {
            this.endpoint = endpoint;
        }

        FitbitDataType(String endpoint, String version) {
            this.endpoint = endpoint;
            this.version = version;
        }

        public String getEndpoint() {

            return endpoint;
        }

        public String getVersion() {
            return version;
        }
    }

    @Override
    protected String getAuthorizationUrl(
            UserRedirectRequiredException exception,
            Map<String, String> additionalParameters) {

        final OAuth2ProtectedResourceDetails resource = getResource();

        // TODO this override won't work, see FitbitAccessTokenRequestEnhancer for details
        String redirectUrl = additionalParameters.get(REDIRECT_URL_KEY) == null
                ? getDefaultRedirectUrl()
                : additionalParameters.get(REDIRECT_URL_KEY);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(exception.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", resource.getClientId())
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("scope", Joiner.on(" ").join(resource.getScope()))
                .queryParam("state", exception.getStateKey())
                .queryParam("prompt", fitbitClientSettings.getPromptType().getQueryParameterValue());

        return uriBuilder.build().encode().toUriString();
    }

    @Override
    public ResponseEntity<ShimDataResponse> getData(
            OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest)
            throws ShimException {

        FitbitDataType fitbitDataType;

        try {
            fitbitDataType = FitbitDataType.valueOf(shimDataRequest.getDataTypeKey().trim().toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e) {

            throw new ShimException("Null or Invalid data type parameter: "
                    + shimDataRequest.getDataTypeKey()
                    + " in shimDataRequest, cannot retrieve data.", e);
        }

        LocalDate today = LocalDate.now();

        LocalDate startDate = shimDataRequest.getStartDateTime() == null
                ? today
                : shimDataRequest.getStartDateTime().toLocalDate();

        LocalDate endDate = shimDataRequest.getEndDateTime() == null
                ? today
                : shimDataRequest.getEndDateTime().toLocalDate();

        if (usesDateRangeQuery(fitbitDataType)) {

            return getDataForDateRange(
                    restTemplate,
                    startDate,
                    endDate,
                    fitbitDataType,
                    shimDataRequest.getNormalize());
        }
        else {
            /*
              Fitbit's API forces you to make a request for each given day of data for some endpoints. Thus we
              make a request for each day in the submitted time range and then aggregate the response based on the
              normalization parameter.
             */
            List<ShimDataResponse> dayResponses = new ArrayList<>();
            LocalDate indexDate = startDate;

            while (!indexDate.isAfter(endDate)) {

                dayResponses.add(getDataForSingleDate(restTemplate, indexDate, fitbitDataType,
                        shimDataRequest.getNormalize()).getBody());

                indexDate = indexDate.plusDays(1);
            }

            return shimDataRequest.getNormalize()
                    ? ok(aggregateNormalized(dayResponses))
                    : ok(aggregateIntoList(dayResponses));
        }
    }

    /**
     * Determines whether a range query should be used for submitting requests based on the data type. Based on the
     * Fitbit API, we are able to use range queries for weight, BMI, and daily step summaries, without losing
     * information needed for schema mapping.
     */
    private boolean usesDateRangeQuery(FitbitDataType fitbitDataType) {

        switch (fitbitDataType) {
            case BODY_MASS_INDEX:
            case BODY_WEIGHT:
            case SLEEP_DURATION:
            case SLEEP_EPISODE:
                return true;

            case HEART_RATE:
            case STEP_COUNT:
                return !fitbitClientSettings.isIntradayDataAvailable();
        }

        return false;
    }

    /**
     * Each 'dayResponse', when normalized, will have a type->list[objects] for the day. So we collect each daily map to
     * create an aggregate map of the full time range.
     */
    @SuppressWarnings("unchecked")
    private ShimDataResponse aggregateNormalized(List<ShimDataResponse> dayResponses) {

        if (dayResponses.isEmpty()) {
            return ShimDataResponse.empty(FitbitShim.SHIM_KEY);
        }

        List<DataPoint> aggregateDataPoints = Lists.newArrayList();

        for (ShimDataResponse dayResponse : dayResponses) {
            if (dayResponse.getBody() != null) {

                List<DataPoint> dayList = (List<DataPoint>) dayResponse.getBody();

                aggregateDataPoints.addAll(dayList);
            }
        }

        return result(FitbitShim.SHIM_KEY, aggregateDataPoints);
    }

    /**
     * Combines all response bodies for each day into a single response.
     *
     * @param dayResponses - daily responses to combine.
     * @return - Combined shim response.
     */
    private ShimDataResponse aggregateIntoList(List<ShimDataResponse> dayResponses) {

        if (CollectionUtils.isEmpty(dayResponses)) {
            return ShimDataResponse.empty(FitbitShim.SHIM_KEY);
        }
        List<Object> responses = new ArrayList<>();
        for (ShimDataResponse dayResponse : dayResponses) {
            if (dayResponse.getBody() != null) {
                responses.add(dayResponse.getBody());
            }
        }
        return responses.size() == 0 ? ShimDataResponse.empty(FitbitShim.SHIM_KEY) :
                result(FitbitShim.SHIM_KEY, responses);
    }

    private ResponseEntity<ShimDataResponse> executeRequest(
            OAuth2RestOperations restTemplate,
            URI requestUri,
            boolean normalize,
            FitbitDataType fitbitDataType,
            LocalDate date)
            throws ShimException {

        ResponseEntity<JsonNode> responseEntity;

        try {
            responseEntity = restTemplate.getForEntity(requestUri, JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // TODO figure out how to handle this
            throw new ShimException("A request for Fitbit data has failed.", e);
        }

        if (normalize) {
            FitbitDataPointMapper<?> dataPointMapper = getDataPointMapper(fitbitDataType);

            List<? extends DataPoint<?>> dataPoints = dataPointMapper.asDataPoints(responseEntity.getBody());

            return ok().body(ShimDataResponse.result(FitbitShim.SHIM_KEY, dataPoints));
        }
        else {

            /*
              For types that only allow us to retrieve a single day at a time, Fitbit does not always provide
              date information since it is assumed we know what date we requested. However, this is problematic
              when we are aggregating multiple single date responses, so we wrap each single day Fitbit data
              point with date information.
             */
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonContent = responseEntity.getBody().toString();

            // TODO replace with proper wrapping
            if (date != null) {
                jsonContent = "{\"result\": {\"date\": \"" + date.toString() + "\" " +
                        ",\"content\": " + responseEntity.getBody().toString() + "}}";
            }

            try {
                return ok().body(ShimDataResponse.result(FitbitShim.SHIM_KEY, objectMapper.readTree(jsonContent)));
            }
            catch (IOException e) {
                throw new ShimException("A Fitbit response doesn't contain valid JSON.", e);
            }
        }
    }

    private FitbitDataPointMapper getDataPointMapper(FitbitDataType fitbitDataType) {

        Integer intradayDataGranularityInMinutes = fitbitClientSettings.getIntradayDataGranularityInMinutes();

        switch (fitbitDataType) {
            case BODY_MASS_INDEX:
                return new FitbitBodyMassIndexDataPointMapper();

            case BODY_WEIGHT:
                return new FitbitBodyWeightDataPointMapper();

            case HEART_RATE:
                if (fitbitClientSettings.isIntradayDataAvailable()) {
                    return new FitbitIntradayHeartRateDataPointMapper(intradayDataGranularityInMinutes);
                }

                throw new UnsupportedOperationException();

            case SLEEP_DURATION:
                return new FitbitSleepDurationDataPointMapper();

            case SLEEP_EPISODE:
                return new FitbitSleepEpisodeDataPointMapper();

            case STEP_COUNT:
                if (fitbitClientSettings.isIntradayDataAvailable()) {
                    return new FitbitIntradayStepCountDataPointMapper(intradayDataGranularityInMinutes);
                }

                return new FitbitStepCountDataPointMapper();

            case PHYSICAL_ACTIVITY:
                return new FitbitPhysicalActivityDataPointMapper();
        }

        throw new UnsupportedOperationException();
    }

    private ResponseEntity<ShimDataResponse> getDataForDateRange(OAuth2RestOperations restTemplate,
            LocalDate startDate,
            LocalDate endDate,
            FitbitDataType dataType,
            boolean normalize) throws ShimException {

        URI url = UriComponentsBuilder
                .fromUriString(DATA_URL)
                .path("/{apiVersion}/user/-/{endpoint}/date/{baseDate}/{endDate}.json")
                .buildAndExpand(dataType.getVersion(), dataType.getEndpoint(), startDate.toString(), endDate.toString())
                .encode()
                .toUri();

        return executeRequest(restTemplate, url, normalize, dataType, null);
    }

    private ResponseEntity<ShimDataResponse> getDataForSingleDate(
            OAuth2RestOperations restTemplate,
            LocalDate date,
            FitbitDataType dataType,
            boolean normalize) throws ShimException {

        String detailLevel = "";

        // TODO generalise this
        if (fitbitClientSettings.isIntradayDataAvailable()) {
            if (dataType == STEP_COUNT || dataType == HEART_RATE) {
                detailLevel = format("/1d/%dmin", fitbitClientSettings.getIntradayDataGranularityInMinutes());
            }
        }

        URI url = UriComponentsBuilder
                .fromUriString(DATA_URL)
                .path("/{apiVersion}/user/-/{endpoint}/date/{date}{detailLevel}.json")
                .buildAndExpand(dataType.getVersion(), dataType.getEndpoint(), date.toString(), detailLevel)
                .encode()
                .toUri();

        return executeRequest(restTemplate, url, normalize, dataType, date);
    }
}
