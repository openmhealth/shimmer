package org.openmhealth.shim.moves;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Splitter;
import org.openmhealth.shim.*;
import org.openmhealth.shim.moves.mapper.MovesDataPointMapper;
import org.openmhealth.shim.moves.mapper.MovesStepCountDataPointMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.http.ResponseEntity.ok;


/**
 * Created by Cheng-Kang Hsieh on 3/3/15.
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.moves")
public class MovesShim extends OAuth2ShimBase{
    private static final Logger logger = LoggerFactory.getLogger(MovesShim.class);

    public static final String SHIM_KEY = "moves";

    private static final String DATA_URL = "https://api.moves-app.com/api/1.1";

    private static final String AUTHORIZE_URL = "https://api.moves-app.com/oauth/v1/authorize";

    private static final String TOKEN_URL = "https://api.moves-app.com/oauth/v1/access_token";


    public static final ArrayList<String> MOVES_SCOPES =
            new ArrayList<>(Arrays.asList(
                    "activity", "location"
            ));

    private static final long MAX_DURATION_IN_DAYS = 31;

    @Autowired
    public MovesShim(ApplicationAccessParametersRepo applicationParametersRepo,
                     AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                     AccessParametersRepo accessParametersRepo,
                     ShimServerConfig shimServerConfig) {
        super(applicationParametersRepo, authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig);
    }

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
    public String getBaseAuthorizeUrl() {
        return AUTHORIZE_URL;
    }

    @Override
    public String getBaseTokenUrl() {
        return TOKEN_URL;
    }

    @Override
    public List<String> getScopes() {
        return MOVES_SCOPES;
    }

    public enum MovesDataType implements ShimDataType {

        STEPS("/user/summary/daily"),
        ACTIVITY("/user/activities/daily");

        private String endPoint;

        private JsonDeserializer<ShimDataResponse> normalizer;

        MovesDataType(String endPoint) {
            this.endPoint = endPoint;
        }

        public String getEndPoint() {
            return endPoint;
        }
    }


    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new MovesTokenRequestEnhancer());
        return provider;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return MovesDataType.values();
    }

    @Override
    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {

        String dataTypeKey = shimDataRequest.getDataTypeKey().trim().toUpperCase();

        MovesDataType movesDataType;
        try {
            movesDataType = MovesDataType.valueOf(dataTypeKey);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                    + dataTypeKey + " in shimDataRequest, cannot retrieve data.");
        }

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

        for (String pathSegment : Splitter.on("/").split(movesDataType.getEndPoint())) {
            uriBuilder.pathSegment(pathSegment);
        }

        uriBuilder
                .queryParam("from", startDateTime.toLocalDate())
                .queryParam("to", endDateTime.toLocalDate())
                .queryParam("trackPoints", false);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + restTemplate.getAccessToken().getValue());
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<JsonNode> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uriBuilder.build().encode().toUri(), HttpMethod.GET, entity, JsonNode.class);
        }
        catch (HttpClientErrorException | HttpServerErrorException e) {
            // FIXME figure out how to handle this
            logger.error("A request for Misfit data failed.", e);
            throw e;
        }

        if (shimDataRequest.getNormalize()) {

            MovesDataPointMapper<?> dataPointMapper;

            switch (movesDataType) {
                case STEPS:
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
    protected String getAuthorizationUrl(UserRedirectRequiredException exception) {
        final OAuth2ProtectedResourceDetails resource = getResource();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(exception.getRedirectUri())
                .queryParam("state", exception.getStateKey())
                .queryParam("client_id", resource.getClientId())
                .queryParam("response_type", "code")
                .queryParam("access_type", "offline")
                .queryParam("approval_prompt", "force")
                .queryParam("scope", StringUtils.collectionToDelimitedString(resource.getScope(), " "))
                .queryParam("redirect_uri", getCallbackUrl());

        return uriBuilder.build().encode().toUriString();
    }

    /**
     * Adds required parameters to authorization token requests.
     */
    private class MovesTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("client_id", resource.getClientId());
            form.set("client_secret", resource.getClientSecret());
            form.set("redirect_uri", getCallbackUrl());
        }
    }
}
