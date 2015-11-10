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

package org.openmhealth.shimmer.common.service;

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
        configProperties.setPaginationRequestSettings(new DefaultPaginationRequestConfigurationProperties());
        configProperties.setPaginationResponseSettings(new UriPaginationResponseConfigurationProperties());

        PaginationRequestEntityAssembler assembler = new PaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new UriPaginationStatus();
        paginationStatus.setPaginationResponseValue("https://api.ihealthlabs.com:8443/openapiv2/fullUri");
        assembler.setPaginationStatus(paginationStatus);

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties));

        assertThat(assembledBuilder.build().getUrl().toString(),
                equalTo(paginationStatus.getPaginationResponseValue().get()));
        assertThat(assembledBuilder.isFinishedAssembling(), is(true));
    }

    @Test
    public void returnsBuilderWithCorrectUriWhenEndpointProvidesPartialUriInResponseThatIsAppendedToTheEnd() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://jawbone.com/nudge/api/v.1.1/users/@me/{endpoint}"));

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();
        configProperties.setPaginationRequestSettings(new DefaultPaginationRequestConfigurationProperties());

        UriPaginationResponseConfigurationProperties paginationResponseConfig =
                new UriPaginationResponseConfigurationProperties();
        paginationResponseConfig.setBaseUri("https://jawbone.com/{paginationResponse}");
        configProperties.setPaginationResponseSettings(paginationResponseConfig);

        PaginationRequestEntityAssembler assembler = new PaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new UriPaginationStatus();
        paginationStatus.setPaginationResponseValue("/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token" +
                "=1440077820&limit=3");
        assembler.setPaginationStatus(paginationStatus);

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties));

        URI expectedUri = UriComponentsBuilder.fromUriString(
                "https://jawbone.com/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token=1440077820&limit=3")
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
        assertThat(assembledBuilder.isFinishedAssembling(), is(false));
    }

    @Test
    public void returnsBuilderWithCorrectQueryParameterWhenEndpointProvidesTokenInResponse() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://www.googleapis.com/fitness/v1/endpointInfo"));

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();

        DefaultPaginationRequestConfigurationProperties paginationRequestConfig =
                new DefaultPaginationRequestConfigurationProperties();
        paginationRequestConfig.setNextPageTokenParameter(createNextPageTokenParameter(QUERY));
        configProperties.setPaginationRequestSettings(paginationRequestConfig);

        configProperties.setPaginationResponseSettings(new TokenPaginationResponseConfigurationProperties());

        PaginationRequestEntityAssembler assembler = new PaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new TokenPaginationStatus();
        paginationStatus.setPaginationResponseValue("1436566038006058105");
        assembler.setPaginationStatus(paginationStatus);

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties));

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

        DefaultPaginationRequestConfigurationProperties paginationRequestConfig =
                new DefaultPaginationRequestConfigurationProperties();
        paginationRequestConfig.setNextPageTokenParameter(createNextPageTokenParameter(PATH));
        configProperties.setPaginationRequestSettings(paginationRequestConfig);

        configProperties.setPaginationResponseSettings(new TokenPaginationResponseConfigurationProperties());

        PaginationRequestEntityAssembler assembler = new PaginationRequestEntityAssembler();

        PaginationStatus paginationStatus = new TokenPaginationStatus();
        paginationStatus.setPaginationResponseValue("1436566038006058105");
        assembler.setPaginationStatus(paginationStatus);

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties));

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

    private void assertThatLimitParameterIsSetCorrectlyWhen(RequestParameterLocation location, String baseUriTemplate,
            String finalUri, Integer defaultValue, Integer maxValue) {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate(baseUriTemplate));

        PaginationRequestEntityAssembler assembler = new PaginationRequestEntityAssembler();

        DefaultEndpointConfigurationProperties configProperties = new DefaultEndpointConfigurationProperties();
        DefaultPaginationRequestConfigurationProperties paginationRequestConfigs =
                new DefaultPaginationRequestConfigurationProperties();
        NumberRequestParameter numberRequestParameter = new NumberRequestParameter();

        if (maxValue != null) {
            numberRequestParameter.setMaximumValue(maxValue);
        }

        if (defaultValue != null) {
            numberRequestParameter.setDefaultValue(defaultValue.doubleValue());
        }

        numberRequestParameter.setRequestParameterLocation(location);
        numberRequestParameter.setParameterName("limit");
        paginationRequestConfigs.setPaginationLimitParameter(numberRequestParameter);
        configProperties.setPaginationRequestSettings(paginationRequestConfigs);

        RequestEntityBuilder assembledBuilder =
                assembler.assemble(builder, createTestDataPointRequest(configProperties));


        URI expectedUri = UriComponentsBuilder.fromUriString(finalUri)
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
    }


    private StringRequestParameter createNextPageTokenParameter(RequestParameterLocation location) {

        StringRequestParameter nextPageTokenParameter = new StringRequestParameter();

        nextPageTokenParameter.setParameterName("pageToken");
        nextPageTokenParameter.setRequestParameterLocation(location);

        return nextPageTokenParameter;
    }

    public DataPointRequest createTestDataPointRequest(EndpointConfigurationProperties configurationProperties) {

        return new DataPointRequest(configurationProperties, "testUser", "testNamespace", "testSchemaName", "1.0");
    }

}
