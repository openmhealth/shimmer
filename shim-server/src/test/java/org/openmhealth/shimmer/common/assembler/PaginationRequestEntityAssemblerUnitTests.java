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

package org.openmhealth.shimmer.common.assembler;

import org.openmhealth.shimmer.common.assembler.PaginationRequestEntityAssembler;
import org.openmhealth.shimmer.common.assembler.TokenPaginationRequestEntityAssembler;
import org.openmhealth.shimmer.common.assembler.UriPaginationRequestEntityAssembler;
import org.openmhealth.shimmer.common.configuration.*;
import org.openmhealth.shimmer.common.domain.DataPointRequest;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.TokenPaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.UriPaginationStatus;
import org.openmhealth.shimmer.common.domain.parameters.NumberRequestParameter;
import org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation;
import org.openmhealth.shimmer.common.domain.parameters.StringRequestParameter;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation.PATH;
import static org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation.QUERY;


/**
 * @author Chris Schaefbauer
 */
public class PaginationRequestEntityAssemblerUnitTests {

    @Test
    public void returnsBuilderWithCorrectUriWhenEndpointProvidesFullPaginationUriInResponse() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://api.ihealthlabs.com:8443/openapiv2/user/{userid}/{endpoint-name}"));

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();
        configProperties.setPaginationSettings(new UriPaginationSettings());

        PaginationRequestEntityAssembler assembler = new UriPaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new UriPaginationStatus();
        paginationStatus.setPaginationResponseValue("https://api.ihealthlabs.com:8443/openapiv2/fullUri");


        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties, paginationStatus));

        assertThat(assembledBuilder.build().getUrl().toString(),
                equalTo(paginationStatus.getPaginationResponseValue().get()));
        assertThat(assembledBuilder.isFinishedAssembling(), is(true));
    }

    @Test
    public void returnsBuilderWithCorrectUriWhenEndpointResponseProvidesPartialUriContainingAnInternalQueryParameter() {

        assertThatPartialUriIsAssembledCorrectlyWhen("/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token" +
                        "=1440077820&limit=3",
                "https://jawbone.com/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token=1440077820&limit=3",
                null);
    }

    @Test
    public void returnsBuilderWithCorrectUriWhenEndpointResponseProvidesPartialUriWithoutInternalQueryParameter() {

        assertThatPartialUriIsAssembledCorrectlyWhen(
                "/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/1436566038006058105",
                "https://jawbone.com/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/1436566038006058105", null);
    }

    @Test
    public void returnsBuilderWithCorrectUriWhenEndpointResponseProvidesPartialUriAndRequiresLimitQueryParameter() {

        NumberRequestParameter limitRequestParameter = createNumberRequestParameter("limit", QUERY, 10, 20);

        assertThatPartialUriIsAssembledCorrectlyWhen("/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token" +
                        "=1440077820",
                "https://jawbone.com/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token=1440077820&limit=20",
                limitRequestParameter);
    }

    @Test
    public void returnsBuilderWithCorrectQueryParameterWhenEndpointProvidesTokenInResponse() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://www.googleapis.com/fitness/v1/endpointInfo"));

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();

        TokenPaginationSettings tokenPaginationSettings = new TokenPaginationSettings();
        tokenPaginationSettings.setNextPageTokenParameter(createNextPageTokenParameter(QUERY));
        configProperties.setPaginationSettings(tokenPaginationSettings);

        PaginationRequestEntityAssembler assembler = new TokenPaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new TokenPaginationStatus();
        paginationStatus.setPaginationResponseValue("1436566038006058105");


        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties, paginationStatus));

        URI expectedUri = UriComponentsBuilder.fromUriString(
                "https://www.googleapis.com/fitness/v1/endpointInfo?pageToken=1436566038006058105")
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
        assertThat(assembledBuilder.isFinishedAssembling(), is(false));
    }

    @Test
    public void returnsBuilderWithCorrectPathParameterWhenEndpointProvidesTokenInResponse() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://www.googleapis.com/fitness/v1/{pageToken}/endpointInfo"));

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();

        TokenPaginationSettings settings = new TokenPaginationSettings();
        settings.setNextPageTokenParameter(createNextPageTokenParameter(PATH));
        configProperties.setPaginationSettings(settings);

        PaginationRequestEntityAssembler assembler = new TokenPaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new TokenPaginationStatus();
        paginationStatus.setPaginationResponseValue("1436566038006058105");

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties, paginationStatus));

        URI expectedUri = UriComponentsBuilder.fromUriString(
                "https://www.googleapis.com/fitness/v1/1436566038006058105/endpointInfo")
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
        assertThat(assembledBuilder.isFinishedAssembling(), is(false));
    }

    @Test
    public void setsLimitQueryParameterToMaxWhenDefaultAndMaxParameterValuesExist() {

        assertThatLimitParameterIsSetCorrectlyWhen(QUERY, "https://api.runkeeper.com/endpoint",
                "https://api.runkeeper.com/endpoint?limit=200", 25, 200);
    }

    @Test
    public void setsLimitPathParameterToMaxWhenDefaultAndMaxParameterValuesExist() {

        assertThatLimitParameterIsSetCorrectlyWhen(PATH, "https://api.runkeeper.com/endpoint/{limit}/",
                "https://api.runkeeper.com/endpoint/200/", 25, 200);
    }

    @Test
    public void doesNotSetLimitParameterWhenDefaultParameterIsMissing() {

        assertThatLimitParameterIsSetCorrectlyWhen(QUERY, "https://api.runkeeper.com/endpoint",
                "https://api.runkeeper.com/endpoint", null, 200);
    }

    @Test
    public void setsLimitQueryParameterToAnArbitrarilyLargeLimitWhenDefaultParameterExistsButMaxDoesNot() {

        assertThatLimitParameterIsSetCorrectlyWhen(QUERY, "https://api.runkeeper.com/endpoint",
                "https://api.runkeeper.com/endpoint?limit=" + PaginationRequestEntityAssembler.ARBITRARILY_LARGE_LIMIT,
                25, null);
    }

    /* Helper classes */

    private void assertThatPartialUriIsAssembledCorrectlyWhen(String partialUriFromResponse, String expectedUriString,
            NumberRequestParameter limitParameter) {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://jawbone.com/nudge/api/v.1.1/users/@me/{endpoint}"));

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();

        UriPaginationSettings paginationSettings =
                new UriPaginationSettings();

        if (limitParameter != null) {
            paginationSettings.setPaginationLimitParameter(limitParameter);
        }

        paginationSettings.setBaseUri("https://jawbone.com/{paginationResponse}");
        configProperties.setPaginationSettings(paginationSettings);

        PaginationRequestEntityAssembler assembler = new UriPaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new UriPaginationStatus();
        paginationStatus.setPaginationResponseValue(partialUriFromResponse);

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties, paginationStatus));

        URI expectedUri = UriComponentsBuilder.fromUriString(
                expectedUriString)
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
        assertThat(assembledBuilder.isFinishedAssembling(), is(false));
    }

    private void assertThatLimitParameterIsSetCorrectlyWhen(RequestParameterLocation location, String baseUriTemplate,
            String finalUri, Integer defaultValue, Integer maxValue) {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate(baseUriTemplate));

        PaginationRequestEntityAssembler assembler = new UriPaginationRequestEntityAssembler();

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();
        BasePaginationSettings paginationSettings = new TokenPaginationSettings();

        NumberRequestParameter numberRequestParameter =
                createNumberRequestParameter("limit", location, defaultValue, maxValue);

        paginationSettings.setPaginationLimitParameter(numberRequestParameter);
        configProperties.setPaginationSettings(paginationSettings);

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties, null));


        URI expectedUri = UriComponentsBuilder.fromUriString(finalUri)
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
    }

    private NumberRequestParameter createNumberRequestParameter(String parameterName, RequestParameterLocation location,
            Integer defaultLimit,
            Integer maxLimit) {

        NumberRequestParameter numberRequestParameter = new NumberRequestParameter();

        numberRequestParameter.setParameterName(parameterName);
        numberRequestParameter.setRequestParameterLocation(location);

        if (defaultLimit != null) {
            numberRequestParameter.setDefaultValue(BigDecimal.valueOf(defaultLimit));
        }

        if (maxLimit != null) {
            numberRequestParameter.setMaximumValue(maxLimit.doubleValue());
        }

        return numberRequestParameter;
    }

    private StringRequestParameter createNextPageTokenParameter(RequestParameterLocation location) {

        StringRequestParameter nextPageTokenParameter = new StringRequestParameter();

        nextPageTokenParameter.setParameterName("pageToken");
        nextPageTokenParameter.setRequestParameterLocation(location);

        return nextPageTokenParameter;
    }

    public DataPointRequest createTestDataPointRequest(EndpointConfigurationProperties configurationProperties,
            PaginationStatus paginationStatus) {

        DataPointRequest dataPointRequest =
                new DataPointRequest(configurationProperties, "testUser", "testNamespace", "testSchemaName", "1.0");

        if (paginationStatus != null) {
            dataPointRequest.setPaginationStatus(paginationStatus);
        }

        return dataPointRequest;
    }

}
