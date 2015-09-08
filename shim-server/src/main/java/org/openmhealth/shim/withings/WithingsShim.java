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

package org.openmhealth.shim.withings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.shim.*;
import org.openmhealth.shim.withings.domain.WithingsBodyMeasureType;
import org.openmhealth.shim.withings.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.singletonList;


/**
 * @author Danilo Bonilla
 * @author Chris Schaefbauer
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.withings")
public class WithingsShim extends OAuth1ShimBase {

    public static final String SHIM_KEY = "withings";

    private static final String DATA_URL = "http://wbsapi.withings.net";

    private static final String REQUEST_TOKEN_URL = "https://oauth.withings.com/account/request_token";

    private static final String AUTHORIZE_URL = "https://oauth.withings.com/account/authorize";

    private static final String TOKEN_URL = "https://oauth.withings.com/account/access_token";

    private static final String PARTNER_ACCESS_ACTIVITY_ENDPOINT = "getintradayactivity";

    @Value("${openmhealth.shim.withings.partner_access:false}")
    protected boolean partnerAccess;

    @Autowired
    public WithingsShim(ApplicationAccessParametersRepo applicationParametersRepo,
            AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
            ShimServerConfig shimServerConfig) {

        super(applicationParametersRepo, authorizationRequestParametersRepo, shimServerConfig);

    }

    @Override
    public String getLabel() {
        return "Withings";
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

    @Override
    public ShimDataType[] getShimDataTypes() {

        return new ShimDataType[] {
                WithingsDataType.HEART_RATE, WithingsDataType.BLOOD_PRESSURE, WithingsDataType.SLEEP,
                WithingsDataType.CALORIES,
                WithingsDataType.HEIGHT, WithingsDataType.STEPS, WithingsDataType.WEIGHT
        };

    }

    @Override
    protected void loadAdditionalAccessParameters(

            HttpServletRequest request, AccessParameters accessParameters) {
        Map<String, Object> addlParams =
                accessParameters.getAdditionalParameters();
        addlParams = addlParams != null ? addlParams : new LinkedHashMap<>();
        // Withings maintains a unique id, separate from username, for each user and requires that as a parameter
        // for requests. Userid is exposed during the authentication process and needed to construct the request URI.
        addlParams.put("userid", request.getParameter("userid"));

    }

    public enum WithingsDataType implements ShimDataType {

        WEIGHT("measure", "getmeas", true),
        HEIGHT("measure", "getmeas", true),
        STEPS("v2/measure", "getactivity", false),
        CALORIES("v2/measure", "getactivity", false),
        SLEEP("v2/sleep", "getsummary", false),
        HEART_RATE("measure", "getmeas", true),
        BLOOD_PRESSURE("measure", "getmeas", true);

        private String endpoint;
        private String measureParameter;
        private boolean usesUnixEpochSecondsDate;

        WithingsDataType(String endpoint, String measureParameter, boolean usesUnixEpochSecondsDate) {
            this.endpoint = endpoint;
            this.measureParameter = measureParameter;
            this.usesUnixEpochSecondsDate = usesUnixEpochSecondsDate;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String getMeasureParameter() {
            return measureParameter;
        }

    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {

        AccessParameters accessParameters = shimDataRequest.getAccessParameters();
        String accessToken = accessParameters.getAccessToken();
        String tokenSecret = accessParameters.getTokenSecret();

        // userid is a unique id associated with each user and returned by Withings in the authorization, this id is
        // used as a parameter in the request
        final String userid = accessParameters.getAdditionalParameters().get("userid").toString();

        final WithingsDataType withingsDataType;
        try {

            withingsDataType = WithingsDataType.valueOf(
                    shimDataRequest.getDataTypeKey().trim().toUpperCase());
        }
        catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                    + shimDataRequest.getDataTypeKey()
                    + " in shimDataRequest, cannot retrieve data.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        URI uri = createWithingsRequestUri(shimDataRequest, userid, withingsDataType);
        URL url = signUrl(uri.toString(), accessToken, tokenSecret, null);

        // TODO: Handle requests for a number of days greater than what Withings supports
        HttpGet get = new HttpGet(url.toString());
        HttpResponse response;
        try {
            response = httpClient.execute(get);
            HttpEntity responseEntity = response.getEntity();

            if (shimDataRequest.getNormalize()) {
                WithingsDataPointMapper mapper;

                switch (withingsDataType) {

                    case WEIGHT:
                        mapper = new WithingsBodyWeightDataPointMapper();
                        break;
                    case HEIGHT:
                        mapper = new WithingsBodyHeightDataPointMapper();
                        break;
                    case STEPS:
                        if (partnerAccess) {
                            // Use a different mapper because the partner-access endpoint generates a different response
                            mapper = new WithingsIntradayStepCountDataPointMapper();
                        }
                        else {
                            mapper = new WithingsDailyStepCountDataPointMapper();
                        }
                        break;
                    case CALORIES:
                        if (partnerAccess) {
                            mapper = new WithingsIntradayCaloriesBurnedDataPointMapper();
                        }
                        else {
                            mapper = new WithingsDailyCaloriesBurnedDataPointMapper();
                        }
                        break;
                    case SLEEP:
                        mapper = new WithingsSleepDurationDataPointMapper();
                        break;
                    case BLOOD_PRESSURE:
                        mapper = new WithingsBloodPressureDataPointMapper();
                        break;
                    case HEART_RATE:
                        mapper = new WithingsHeartRateDataPointMapper();
                        break;
                    default:
                        throw new UnsupportedOperationException();

                }

                InputStream content = responseEntity.getContent();
                JsonNode jsonNode = objectMapper.readValue(content, JsonNode.class);
                List<DataPoint> dataPoints = mapper.asDataPoints(singletonList(jsonNode));
                return ShimDataResponse.result(WithingsShim.SHIM_KEY,
                        dataPoints);
            }
            else {
                return ShimDataResponse
                        .result(WithingsShim.SHIM_KEY, objectMapper.readTree(responseEntity.getContent()));
            }

        }
        catch (IOException e) {
            throw new ShimException("Could not fetch data", e);
        }
        finally {
            get.releaseConnection();
        }
    }

    URI createWithingsRequestUri(ShimDataRequest shimDataRequest, String userid,
            WithingsDataType withingsDataType) {

        MultiValueMap<String, String> dateTimeMap = new LinkedMultiValueMap<>();
        if (withingsDataType.usesUnixEpochSecondsDate || isPartnerAccessActivityMeasure(withingsDataType)) {
            //the partner access endpoints for activity also use epoch secs

            dateTimeMap.add("startdate", String.valueOf(shimDataRequest.getStartDateTime().toEpochSecond()));
            dateTimeMap.add("enddate", String.valueOf(shimDataRequest.getEndDateTime().plusDays(1).toEpochSecond()));
        }
        else {
            dateTimeMap.add("startdateymd", shimDataRequest.getStartDateTime().toLocalDate().toString());
            dateTimeMap.add("enddateymd", shimDataRequest.getEndDateTime().toLocalDate().toString());

        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(DATA_URL).pathSegment(
                withingsDataType.getEndpoint());
        String measureParameter;
        if (isPartnerAccessActivityMeasure(withingsDataType)) {
            // partner level access allows greater detail around activity, but uses a different endpoint
            measureParameter = PARTNER_ACCESS_ACTIVITY_ENDPOINT;
        }
        else {
            measureParameter = withingsDataType.getMeasureParameter();
        }
        uriComponentsBuilder.queryParam("action", measureParameter).queryParam("userid", userid)
                .queryParams(dateTimeMap);

        // if it's a body measure
        if (Objects.equals(withingsDataType.getMeasureParameter(), "getmeas")) {

            /*
                The Withings API allows us to query for single body measures, which we take advantage of to reduce
                unnecessary data transfer. However, since blood pressure is represented as two separate measures,
                namely a diastolic and a systolic measure, when the measure type is blood pressure we ask for all
                measures and then filter out the ones we don't care about.
             */
            if (withingsDataType != WithingsDataType.BLOOD_PRESSURE) {

                WithingsBodyMeasureType measureType = WithingsBodyMeasureType.valueOf(withingsDataType.name());
                uriComponentsBuilder.queryParam("meastype", measureType.getMagicNumber());
            }

            uriComponentsBuilder.queryParam("category", 1); //filter out goal datapoints

        }

        UriComponents uriComponents = uriComponentsBuilder.build();
        return uriComponents.toUri();

    }

    /**
     * Determines whether the request is a Withings partner-access level activity request based on the configuration
     * setup and the data type from the Shim API request. This case requires a different endpoint and different time
     * parameters than the standard activity endpoint.
     *
     * @param withingsDataType the withings data type retrieved from the Shim API request
     */
    private boolean isPartnerAccessActivityMeasure(WithingsDataType withingsDataType) {

        return (partnerAccess &&
                (withingsDataType == WithingsDataType.STEPS || withingsDataType == WithingsDataType.CALORIES));

    }
}
