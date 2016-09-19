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

package org.openmhealth.shim.microsoft;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.openmhealth.shim.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;
import org.openmhealth.shim.microsoft.mapper.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;


/**
 * @author Juanjo Campana
 * @author Wallace Wadge
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.microsoft")
public class MicrosoftShim extends OAuth2ShimBase {

    private static final Logger logger = getLogger(MicrosoftShim.class);

    public static final String SHIM_KEY = "microsoft";

    private static final String DATA_URL = "https://api.microsofthealth.net/v1/me";

    private static final String AUTHORIZE_URL = "https://login.live.com/oauth20_authorize.srf";

    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";

    public static final List<String> MICROSOFT_SCOPES = Arrays.asList("mshealth.ReadDevices", "mshealth.ReadActivityHistory", "mshealth.ReadActivityLocation", "offline_access");

    private static final long MAX_DURATION_IN_DAYS = 31;

    @Autowired
    public MicrosoftShim(ApplicationAccessParametersRepo applicationParametersRepo,
                         AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                         AccessParametersRepo accessParametersRepo,
                         ShimServerConfig shimServerConfig) {
        super(applicationParametersRepo, authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig);
    }

    @Override
    public String getLabel() {
        return "Microsoft";
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
        return MICROSOFT_SCOPES;
    }

    @Override
    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        return new MicrosoftAuthorizationCodeAccessTokenProvider();
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return MicrosoftDataTypes.values();
    }

    public enum MicrosoftDataTypes implements ShimDataType {


        ACTIVITY("Activities/", new MicrosoftPhysicalActivityDataPointMapper(), true),
        CALORIES("Summaries/Daily", new MicrosoftCaloriesBurnedDataPointMapper(), true),
        HEARTRATE("Summaries/Daily", new MicrosoftHeartRateDataPointMapper(), true),
        SLEEP("Activities/", new MicrosoftSleepDurationDataPointMapper(), false),
        STEPS("Summaries/Daily", new MicrosoftStepCountDataPointMapper(), true);

        private final boolean dateTimeBound;
        private JsonNodeDataPointMapper mapper;
        private String endPoint;

        <T extends MicrosoftDataPointMapper> MicrosoftDataTypes(String endPoint, T mapper, boolean dateTimeBound) {
            this.endPoint = endPoint;
            this.mapper = mapper;
            this.dateTimeBound = dateTimeBound;
        }

        public boolean isDateTimeBound() {
            return this.dateTimeBound;
        }

        public JsonNodeDataPointMapper getMapper() {
            return mapper;
        }

        public String getEndPoint() {
            return endPoint;
        }
    }

    @Override
    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {

        final MicrosoftDataTypes microsoftDataType;
        try {
            microsoftDataType = MicrosoftDataTypes.valueOf(shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
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
                    startDateTime.plusDays(MAX_DURATION_IN_DAYS - 1);
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(DATA_URL);

        for (String pathSegment : Splitter.on("/").split(microsoftDataType.getEndPoint())) {
            uriBuilder.pathSegment(pathSegment);
        }
        if (microsoftDataType.isDateTimeBound()) {

            uriBuilder
                    .queryParam("startTime", startDateTime.toInstant()) // TODO convert ODT to LocalDate properly
                    .queryParam("endTime", endDateTime.toInstant());

        }
        if (microsoftDataType == MicrosoftDataTypes.SLEEP) {

            uriBuilder.queryParam("activityTypes", "Sleep");
        }
        ResponseEntity<JsonNode> responseEntity;

        try {
            responseEntity = restTemplate.getForEntity(uriBuilder.build().encode().toUri(), JsonNode.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("A request for Microsoft data failed.", e);
            logger.error(e.getResponseBodyAsString());
            throw e;
        }

        if (shimDataRequest.getNormalize()) {
            return ok().body(ShimDataResponse.result(SHIM_KEY,
                    microsoftDataType.getMapper().asDataPoints(singletonList(responseEntity.getBody()))));
        } else {
            return ok().body(ShimDataResponse.result(SHIM_KEY, responseEntity.getBody()));
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
                .queryParam("scope", Joiner.on(',').join(resource.getScope()))
                .queryParam("redirect_uri", getCallbackUrl());
        return uriBuilder.build().encode().toUriString();
    }

    /**
     * Simple overrides to base spring class from oauth.
     */
    public class MicrosoftAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {

        public MicrosoftAuthorizationCodeAccessTokenProvider() {
            this.setTokenRequestEnhancer(new MicrosoftTokenRequestEnhancer());
        }
    }


    /**
     * Adds microsoft required parameters to authorization token requests.
     */
    private class MicrosoftTokenRequestEnhancer implements RequestEnhancer {

        @Override
        public void enhance(AccessTokenRequest request, OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("redirect_uri", getCallbackUrl());
        }
    }
}
