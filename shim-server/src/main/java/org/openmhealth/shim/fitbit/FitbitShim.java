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

package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.shim.*;
import org.openmhealth.shim.fitbit.mapper.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.openmhealth.shim.ShimDataResponse.result;
import static org.openmhealth.shim.fitbit.FitbitShim.FitbitDataType.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;


/**
 * @author Danilo Bonilla
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.fitbit")
public class FitbitShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "fitbit";

    private static final String DATA_URL = "https://api.fitbit.com";

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth2/authorize";

    private static final String TOKEN_URL = "https://api.fitbit.com/oauth2/token";

    private static final Logger logger = getLogger(FitbitShim.class);

    public static final List<String> FITBIT_SCOPES = Arrays.asList("activity", "heartrate", "sleep", "weight");

    @Value("${openmhealth.shim.fitbit.partnerAccess:false}")
    protected boolean partnerAccess;

    @Autowired
    public FitbitShim(ApplicationAccessParametersRepo applicationParametersRepo,
            AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
            AccessParametersRepo accessParametersRepo,
            ShimServerConfig shimServerConfig) {
        super(applicationParametersRepo, authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig);
    }

    @Override
    public String getLabel() {
        return "Fitbit";
    }

    @Override
    public List<String> getScopes() {
        return FITBIT_SCOPES;
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
    public ShimDataType[] getShimDataTypes() {
        return values();
    }

    @Override
    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new FitbitAuthorizationCodeAccessTokenProvider();
    }

    public enum FitbitDataType implements ShimDataType {

        WEIGHT("body/log/weight"),
        SLEEP("sleep"),
        BODY_MASS_INDEX("body/log/weight"),
        STEPS("activities/steps"),
        ACTIVITY("activities");

        private String endPoint;

        FitbitDataType(String endPoint) {

            this.endPoint = endPoint;
        }

        public String getEndPoint() {
            return endPoint;
        }

    }

    @Override
    protected String getAuthorizationUrl(UserRedirectRequiredException exception) {

        final OAuth2ProtectedResourceDetails resource = getResource();

        String callbackUri = getCallbackUrl();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(exception.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", resource.getClientId())
                .queryParam("redirect_uri", callbackUri)
                .queryParam("scope", StringUtils.collectionToDelimitedString(resource.getScope(), " "))
                .queryParam("state", exception.getStateKey())
                .queryParam("prompt", "login");

        return uriBuilder.build().encode().toUriString();
    }

    @Override
    public ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
            ShimDataRequest shimDataRequest) throws ShimException {

        FitbitDataType fitbitDataType;

        try {
            fitbitDataType = valueOf(shimDataRequest.getDataTypeKey().trim().toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e) {

            throw new ShimException("Null or Invalid data type parameter: "
                    + shimDataRequest.getDataTypeKey()
                    + " in shimDataRequest, cannot retrieve data.");
        }

        /***
         * Setup default date parameters
         */
        OffsetDateTime today = LocalDate.now().atStartOfDay(ZoneId.of("Z")).toOffsetDateTime();

        OffsetDateTime startDate = shimDataRequest.getStartDateTime() == null ?
                today.minusDays(1) : shimDataRequest.getStartDateTime().toOffsetDateTime();

        OffsetDateTime endDate = shimDataRequest.getEndDateTime() == null ?
                today.plusDays(1) : shimDataRequest.getEndDateTime().toOffsetDateTime();

        OffsetDateTime currentDate = startDate;

        if (usesDateRangeQuery(fitbitDataType)) {
    		if(fitbitDataType != fitbitDataType.WEIGHT) {
	            return getDataForDateRange(restTemplate,
	                    startDate, endDate, fitbitDataType,
	                    shimDataRequest.getNormalize());
    		}
    		else{
    			/**
                 * Fitbit's API limits you to making a request for only 31 days. So when a request is made with a larger date
                 * range than 31 days, we split it up and make multiple requests.
                 */
    			OffsetDateTime tempEndDate = endDate;
            	long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(currentDate, endDate);
            	if(daysBetween > 31){
            		tempEndDate = currentDate.plusDays(30);
            	}
            	
    			List<ShimDataResponse> responses = new ArrayList<>();

                while (currentDate.toLocalDate().isBefore(endDate.toLocalDate()) ||
                        currentDate.toLocalDate().isEqual(endDate.toLocalDate())) {

                    responses.add(getDataForDateRange(restTemplate,
                    		currentDate, tempEndDate, fitbitDataType,
    	                    shimDataRequest.getNormalize()).getBody());
                    currentDate = currentDate.plusDays(30);
                    tempEndDate = currentDate.plusDays(30);
                    if(tempEndDate.isAfter(endDate)){ tempEndDate = endDate; }
                }

                return shimDataRequest.getNormalize() ?
                        ok(aggregateNormalized(responses))
                        : ok(aggregateIntoList(responses));
    		}
        }
        else {
            /**
             * Fitbit's API limits you to making a request for each given day of data for some endpoints. Thus we
             * make a request for each day in the submitted time range and then aggregate the response based on the
             * normalization parameter.
             */
            List<ShimDataResponse> dayResponses = new ArrayList<>();

            while (currentDate.toLocalDate().isBefore(endDate.toLocalDate()) ||
                    currentDate.toLocalDate().isEqual(endDate.toLocalDate())) {

                dayResponses.add(getDataForSingleDate(restTemplate, currentDate, fitbitDataType,
                        shimDataRequest.getNormalize()).getBody());
                currentDate = currentDate.plusDays(1);
            }

            return shimDataRequest.getNormalize() ?
                    ok(aggregateNormalized(dayResponses))
                    : ok(aggregateIntoList(dayResponses));
        }
    }

    /**
     * Determines whether a range query should be used for submitting requests based on the data type. Based on the
     * Fitbit API, we are able to use range queries for weight, BMI, and daily step summaries, without losing
     * information needed for schema mapping.
     */
    private boolean usesDateRangeQuery(FitbitDataType fitbitDataType) {

        // partnerAccess means that intraday steps will be used, which are not accessible from a ranged query
        return fitbitDataType.equals(WEIGHT)
                || fitbitDataType.equals(BODY_MASS_INDEX)
                || (fitbitDataType.equals(STEPS) && !partnerAccess);
    }

    /**
     * Each 'dayResponse', when normalized, will have a type->list[objects] for the day. So we collect each daily map
     * to create an aggregate map of the full time range.
     */
    @SuppressWarnings("unchecked")
    private ShimDataResponse aggregateNormalized(List<ShimDataResponse> dayResponses) {

        if (CollectionUtils.isEmpty(dayResponses)) {
            return ShimDataResponse.empty(FitbitShim.SHIM_KEY);
        }

        List<DataPoint> aggregateDataPoints = Lists.newArrayList();

        for (ShimDataResponse dayResponse : dayResponses) {
            if (dayResponse.getBody() != null) {

                List<DataPoint> dayList = (List<DataPoint>) dayResponse.getBody();

                for (DataPoint dataPoint : dayList) {

                    aggregateDataPoints.add(dataPoint);
                }

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
            String dateString
    ) throws ShimException {

        ResponseEntity<JsonNode> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(requestUri, JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // FIXME figure out how to handle this
            logger.error("A request for Fitbit data failed.", e);
            throw e;
        }

        if (normalize) {

            FitbitDataPointMapper dataPointMapper;

            switch ( fitbitDataType ) {
                case STEPS:
                    if (partnerAccess) {
                        dataPointMapper = new FitbitIntradayStepCountDataPointMapper();
                    }
                    else {
                        dataPointMapper = new FitbitStepCountDataPointMapper();
                    }
                    break;
                case ACTIVITY:
                    dataPointMapper = new FitbitPhysicalActivityDataPointMapper();
                    break;
                case WEIGHT:
                    dataPointMapper = new FitbitBodyWeightDataPointMapper();
                    break;
                case SLEEP:
                    dataPointMapper = new FitbitSleepDurationDataPointMapper();
                    break;
                case BODY_MASS_INDEX:
                    dataPointMapper = new FitbitBodyMassIndexDataPointMapper();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            return ok().body(result(FitbitShim.SHIM_KEY,
                    dataPointMapper.asDataPoints(singletonList(responseEntity.getBody()))));

        }
        else {

            /**
             * For types that only allow us to retrieve a single day at a time, Fitbit does not always provide
             * date information since it is assumed we know what date we requested. However, this is problematic
             * when we are aggregating multiple single date responses, so we wrap each single day Fitbit data
             * point with date information.
             */

            ObjectMapper objectMapper = new ObjectMapper();

            String jsonContent = responseEntity.getBody().toString();

            if (dateString != null) {
                jsonContent = "{\"result\": {\"date\": \"" + dateString + "\" " +
                        ",\"content\": " + responseEntity.getBody().toString() + "}}";
            }

            try {
                return ok().body(ShimDataResponse.result(FitbitShim.SHIM_KEY, objectMapper.readTree(jsonContent)));
            }
            catch (IOException e) {
                logger.error("Data returned in Fitbit request was not valid JSON.", e);
                throw new ShimException("Data returned in Fitbit request was not valid JSON");
            }
        }
    }

    private ResponseEntity<ShimDataResponse> getDataForDateRange(OAuth2RestOperations restTemplate,
            OffsetDateTime fromTime,
            OffsetDateTime toTime,
            FitbitDataType fitbitDataType,
            boolean normalize) throws ShimException {

        String fromDateString = fromTime.toLocalDate().toString();
        String toDateString = toTime.toLocalDate().toString();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(DATA_URL).
                path("/1/user/-/{fitbitDataTypeEndpoint}/date/{fromDateString}/{toDateString}.json");

        URI requestUri =
                uriComponentsBuilder.buildAndExpand(fitbitDataType.getEndPoint(), fromDateString, toDateString).encode()
                        .toUri();

        return executeRequest(restTemplate, requestUri, normalize, fitbitDataType, null);
    }

    private ResponseEntity<ShimDataResponse> getDataForSingleDate(OAuth2RestOperations restTemplate,
            OffsetDateTime dateTime,
            FitbitDataType fitbitDataType,
            boolean normalize) throws ShimException {

        String dateString = dateTime.toLocalDate().toString();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(DATA_URL).
                path("/1/user/-/{fitbitDataTypeEndpoint}/date/{dateString}{stepTimeSeries}.json");

        URI endpointUrl = uriComponentsBuilder.buildAndExpand(fitbitDataType.getEndPoint(), dateString,
                (fitbitDataType == STEPS ? "/1d/1min" : "")).encode().toUri();

        return executeRequest(restTemplate, endpointUrl, normalize, fitbitDataType, dateString);
    }

    public class FitbitAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {

        public FitbitAuthorizationCodeAccessTokenProvider() {
            this.setTokenRequestEnhancer(new FitbitTokenRequestEnhancer());
        }

        @Override
        protected HttpMethod getHttpMethod() {
            return HttpMethod.POST;
        }
    }


    private class FitbitTokenRequestEnhancer implements RequestEnhancer {

        @Override
        public void enhance(AccessTokenRequest request, OAuth2ProtectedResourceDetails resource,
                MultiValueMap<String, String> form, HttpHeaders headers) {

            form.set("client_id", resource.getClientId());
            form.set("redirect_uri", getCallbackUrl());
        }
    }
}
