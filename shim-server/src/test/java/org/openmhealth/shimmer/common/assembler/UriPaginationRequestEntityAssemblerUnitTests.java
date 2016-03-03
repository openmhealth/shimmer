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

import org.openmhealth.shimmer.common.configuration.DefaultEndpointSettings;
import org.openmhealth.shimmer.common.configuration.UriPaginationSettings;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.UriPaginationStatus;
import org.openmhealth.shimmer.common.domain.parameters.NumberRequestParameter;
import org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation;
import org.openmhealth.shimmer.common.domain.parameters.StringRequestParameter;
import org.springframework.http.RequestEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation.PATH_VARIABLE;
import static org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation.QUERY_PARAMETER;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationRequestEntityAssemblerUnitTests extends PaginationRequestEntityAssemblerUnitTests {


    @Test
    public void returnsCorrectUriWhenEndpointProvidesFullPaginationUriInResponse() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://api.ihealthlabs.com:8443/openapiv2/user/{userid}/{endpoint-name}"));

        DefaultEndpointSettings endpointSettings = new DefaultEndpointSettings();
        endpointSettings.setPaginationSettings(new UriPaginationSettings());

        // In this case, having uri pagination settings without a base uri means that it returns the full URI
        UriPaginationRequestEntityAssembler uriAssembler =
                new UriPaginationRequestEntityAssembler(new UriPaginationSettings());

        PaginationStatus paginationStatus = new UriPaginationStatus();
        paginationStatus.setPaginationResponseValue("https://api.ihealthlabs.com:8443/openapiv2/fullUri");

        RequestEntity request =
                uriAssembler.assemble(builder, createTestDataPointRequest(endpointSettings, paginationStatus)).build();

        assertThat(request.getUrl().toString(), equalTo("https://api.ihealthlabs.com:8443/openapiv2/fullUri"));
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

        NumberRequestParameter limitRequestParameter = createNumberRequestParameter("limit", QUERY_PARAMETER, 10, 20);

        assertThatPartialUriIsAssembledCorrectlyWhen("/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token" +
                        "=1440077820",
                "https://jawbone.com/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token=1440077820&limit=20",
                limitRequestParameter);
    }

    @Test
    public void setsLimitQueryParameterToMaxWhenDefaultAndMaxParameterValuesExist() {

        assertThatLimitParameterIsSetCorrectlyWhen(QUERY_PARAMETER, "https://api.runkeeper.com/endpoint",
                "https://api.runkeeper.com/endpoint?limit=200", 25, 200);
    }

    @Test
    public void setsLimitPathParameterToMaxWhenDefaultAndMaxParameterValuesExist() {

        assertThatLimitParameterIsSetCorrectlyWhen(PATH_VARIABLE, "https://api.runkeeper.com/endpoint/{limit}/",
                "https://api.runkeeper.com/endpoint/200/", 25, 200);
    }

    @Test
    public void doesNotSetLimitParameterWhenDefaultParameterIsMissing() {

        assertThatLimitParameterIsSetCorrectlyWhen(QUERY_PARAMETER, "https://api.runkeeper.com/endpoint",
                "https://api.runkeeper.com/endpoint", null, 200);
    }

    @Test
    public void setsLimitQueryParameterToAnArbitrarilyLargeLimitWhenDefaultParameterExistsButMaxDoesNot() {

        assertThatLimitParameterIsSetCorrectlyWhen(QUERY_PARAMETER, "https://api.runkeeper.com/endpoint",
                "https://api.runkeeper.com/endpoint?limit=" + PaginationRequestEntityAssembler.ARBITRARILY_LARGE_LIMIT,
                25, null);
    }

    /* Helper classes */

    private void assertThatPartialUriIsAssembledCorrectlyWhen(String partialUriFromResponse, String expectedUriString,
            NumberRequestParameter limitParameter) {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://jawbone.com/nudge/api/v.1.1/users/@me/{endpoint}"));

        DefaultEndpointSettings configProperties = new DefaultEndpointSettings();

        UriPaginationSettings paginationSettings =
                new UriPaginationSettings();

        if (limitParameter != null) {
            paginationSettings.setPaginationLimitParameter(limitParameter);
        }

        paginationSettings.setBaseUri("https://jawbone.com/{paginationResponse}");

        StringRequestParameter paginationParameter = new StringRequestParameter();

        paginationParameter.setName("paginationResponse");
        paginationParameter.setLocation(PATH_VARIABLE);

        paginationSettings.setNextPageParameter(paginationParameter);

        configProperties.setPaginationSettings(paginationSettings);

        PaginationStatus paginationStatus = new UriPaginationStatus();
        paginationStatus.setPaginationResponseValue(partialUriFromResponse);

        UriPaginationRequestEntityAssembler uriAssembler = new UriPaginationRequestEntityAssembler(paginationSettings);

        RequestEntityBuilder assembledBuilder =
                uriAssembler.assemble(builder, createTestDataPointRequest(configProperties, paginationStatus));

        URI expectedUri = UriComponentsBuilder.fromUriString(expectedUriString).build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
    }

    private void assertThatLimitParameterIsSetCorrectlyWhen(RequestParameterLocation location, String baseUriTemplate,
            String finalUri, Integer defaultValue, Integer maxValue) {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate(baseUriTemplate));

        DefaultEndpointSettings configProperties = new DefaultEndpointSettings();
        UriPaginationSettings paginationSettings = new UriPaginationSettings();

        NumberRequestParameter numberRequestParameter =
                createNumberRequestParameter("limit", location, defaultValue, maxValue);

        paginationSettings.setPaginationLimitParameter(numberRequestParameter);
        configProperties.setPaginationSettings(paginationSettings);

        UriPaginationRequestEntityAssembler uriAssembler = new UriPaginationRequestEntityAssembler(paginationSettings);

        RequestEntityBuilder assembledBuilder =
                uriAssembler.assemble(builder, createTestDataPointRequest(configProperties, null));


        URI expectedUri = UriComponentsBuilder.fromUriString(finalUri)
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
    }

    private NumberRequestParameter createNumberRequestParameter(String parameterName, RequestParameterLocation location,
            Integer defaultLimit,
            Integer maxLimit) {

        NumberRequestParameter numberRequestParameter = new NumberRequestParameter();

        numberRequestParameter.setName(parameterName);
        numberRequestParameter.setLocation(location);

        if (defaultLimit != null) {
            numberRequestParameter.setDefaultValue(BigDecimal.valueOf(defaultLimit));
        }

        if (maxLimit != null) {
            numberRequestParameter.setMaximumValue(maxLimit.doubleValue());
        }

        return numberRequestParameter;
    }

}
