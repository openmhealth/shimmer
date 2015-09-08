/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.shim.*;
import org.openmhealth.shim.fitbit.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static java.util.Collections.singletonList;


/**
 * @author Danilo Bonilla
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.fitbit")
public class FitbitShim extends OAuth1ShimBase {

    public static final String SHIM_KEY = "fitbit";

    private static final String DATA_URL = "https://api.fitbit.com";

    private static final String REQUEST_TOKEN_URL = "https://api.fitbit.com/oauth/request_token";

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth/authenticate";

    private static final String TOKEN_URL = "https://api.fitbit.com/oauth/access_token";

    @Autowired
    public FitbitShim(ApplicationAccessParametersRepo applicationParametersRepo,
            AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
            ShimServerConfig shimServerConfig) {

        super(applicationParametersRepo, authorizationRequestParametersRepo, shimServerConfig);
    }

    @Override
    public String getLabel() {
        return "Fitbit";
    }

    @Override
    public List<String> getScopes() {
        return null; //noop!
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getBaseRequestTokenUrl() {
        return REQUEST_TOKEN_URL;
    }

    @Override
    public String getBaseAuthorizeUrl() {
        return AUTHORIZE_URL;
    }

    @Override
    public String getBaseTokenUrl() {
        return TOKEN_URL;
    }

    protected HttpMethod getRequestTokenMethod() {
        return HttpMethod.POST;
    }

    protected HttpMethod getAccessTokenMethod() {
        return HttpMethod.POST;
    }

    private static DateTimeFormatter formatterMins =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZoneUTC();

    private static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    public ShimDataType[] getShimDataTypes() {
        return FitbitDataType.values();
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
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {

        AccessParameters accessParameters = shimDataRequest.getAccessParameters();
        String accessToken = accessParameters.getAccessToken();
        String tokenSecret = accessParameters.getTokenSecret();

        FitbitDataType fitbitDataType;
        try {
            fitbitDataType = FitbitDataType.valueOf(shimDataRequest.getDataTypeKey().trim().toUpperCase());
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
        //dayFormatter.parseDateTime(new DateTime().toString(dayFormatter)); //ensure beginning of today

        OffsetDateTime startDate = shimDataRequest.getStartDateTime() == null ?
                today.minusDays(1) : shimDataRequest.getStartDateTime();

        OffsetDateTime endDate = shimDataRequest.getEndDateTime() == null ?
                today.plusDays(1) : shimDataRequest.getEndDateTime();

        OffsetDateTime currentDate = startDate;

        if (fitbitDataType.equals(FitbitDataType.WEIGHT)) {
            return getRangeData(
                    startDate, endDate, fitbitDataType,
                    shimDataRequest.getNormalize(), accessToken, tokenSecret);
        }
        else {
            /**
             * Fitbit's API limits you to making a request for each given day
             * of data. Thus we make a request for each day in the submitted time
             * range and then aggregate the response based on the normalization parameter.
             */
            List<ShimDataResponse> dayResponses = new ArrayList<>();

            while (currentDate.toLocalDate().isBefore(endDate.toLocalDate()) ||
                    currentDate.toLocalDate().isEqual(endDate.toLocalDate())) {

                dayResponses.add(getDaysData(currentDate, fitbitDataType,
                        shimDataRequest.getNormalize(), accessToken, tokenSecret));
                currentDate = currentDate.plusDays(1);
            }

            ShimDataResponse shimDataResponse = shimDataRequest.getNormalize() ?
                    aggregateNormalized(dayResponses) : aggregateIntoList(dayResponses);
            return shimDataResponse;
        }
    }

    /**
     * Each 'dayResponse', when normalized, will have a type->list[objects] for the day.
     * So we collect each daily map to create an aggregate map of the full
     * time range.
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
        return aggregateDataPoints.size() == 0 ?
                ShimDataResponse.empty(FitbitShim.SHIM_KEY) :
                ShimDataResponse.result(FitbitShim.SHIM_KEY, aggregateDataPoints);
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
                ShimDataResponse.result(FitbitShim.SHIM_KEY, responses);
    }

    private ShimDataResponse executeRequest(String endPointUrl,
            String accessToken,
            String tokenSecret,
            boolean normalize,
            FitbitDataType fitbitDataType
    ) throws ShimException {

        ApplicationAccessParameters parameters = findApplicationAccessParameters();
        HttpRequestBase dataRequest =
                OAuth1Utils.getSignedRequest(HttpMethod.GET,
                        endPointUrl, parameters.getClientId(), parameters.getClientSecret(), accessToken, tokenSecret,
                        null);

        HttpResponse response;
        try {
            response = httpClient.execute(dataRequest);
            HttpEntity responseEntity = response.getEntity();

            StringWriter writer = new StringWriter();
            IOUtils.copy(responseEntity.getContent(), writer);

            String jsonContent = writer.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            if (normalize) {

                IOUtils.copy(responseEntity.getContent(), writer);

                JsonNode jsonNode = objectMapper.readValue(writer.toString(), JsonNode.class);

                FitbitDataPointMapper dataPointMapper;

                switch ( fitbitDataType ) {
                    case STEPS:
                        dataPointMapper = new FitbitStepCountDataPointMapper();
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

                return ShimDataResponse
                        .result(FitbitShim.SHIM_KEY, dataPointMapper.asDataPoints(singletonList(jsonNode)));

            }
            else {
                return ShimDataResponse.result(FitbitShim.SHIM_KEY,
                        objectMapper.readTree(jsonContent));
            }
        }
        catch (IOException e) {
            throw new ShimException("Could not fetch data", e);
        }
        finally {
            dataRequest.releaseConnection();
        }
    }

    private ShimDataResponse getRangeData(OffsetDateTime fromTime,
            OffsetDateTime toTime,
            FitbitDataType fitbitDataType,
            boolean normalize,
            String accessToken, String tokenSecret) throws ShimException {

        String fromDateString = fromTime.toLocalDate().toString();
        String toDateString = toTime.toLocalDate().toString();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(DATA_URL).
                path("/1/user/-/{fitbitDataTypeEndpoint}/date/{fromDateString}/{toDateString}{stepTimeSeries}.json");
        String endpointUrl = uriComponentsBuilder.buildAndExpand(fitbitDataType.getEndPoint(), fromDateString, toDateString,
                (fitbitDataType == FitbitDataType.STEPS ? "/1d/1min" : "")).encode().toUriString();

        return executeRequest(endpointUrl, accessToken, tokenSecret, normalize, fitbitDataType);
    }

    private ShimDataResponse getDaysData(OffsetDateTime dateTime,
            FitbitDataType fitbitDataType,
            boolean normalize,
            String accessToken, String tokenSecret) throws ShimException {

        String dateString = dateTime.toLocalDate().toString();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(DATA_URL).
                path("/1/user/-/{fitbitDataTypeEndpoint}/date/{dateString}{stepTimeSeries}.json");
        String endpointUrl = uriComponentsBuilder.buildAndExpand(fitbitDataType.getEndPoint(), dateString,
                (fitbitDataType == FitbitDataType.STEPS ? "/1d/1min" : "")).encode().toString();

        ApplicationAccessParameters parameters = findApplicationAccessParameters();
        HttpRequestBase dataRequest =
                OAuth1Utils.getSignedRequest(HttpMethod.GET,
                        endpointUrl, parameters.getClientId(), parameters.getClientSecret(), accessToken, tokenSecret,
                        null);

        HttpResponse response;
        try {
            response = httpClient.execute(dataRequest);
            HttpEntity responseEntity = response.getEntity();

            StringWriter writer = new StringWriter();
            IOUtils.copy(responseEntity.getContent(), writer);

            ObjectMapper objectMapper = new ObjectMapper();

            if (normalize) {
                
                JsonNode jsonNode = objectMapper.readValue(writer.toString(), JsonNode.class);

                FitbitDataPointMapper dataPointMapper;

                switch ( fitbitDataType ) {
                    case STEPS:
                        dataPointMapper = new FitbitStepCountDataPointMapper();
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

                return ShimDataResponse
                        .result(FitbitShim.SHIM_KEY, dataPointMapper.asDataPoints(singletonList(jsonNode)));

            }
            else {
                /**
                 * The fitbit API's system works by retrieving each day's
                 * data. The date captured is not returned in the data from fitbit because
                 * it's implicit so we create a JSON wrapper that includes it.
                 */
                String jsonContent = "{\"result\": {\"date\": \"" + dateString + "\" " +
                        ",\"content\": " + writer.toString() + "}}";

                return ShimDataResponse.result(FitbitShim.SHIM_KEY,
                        objectMapper.readTree(jsonContent));
            }
        }
        catch (IOException e) {
            throw new ShimException("Could not fetch data", e);
        }
        finally {
            dataRequest.releaseConnection();
        }
    }
}
