/*
 * Copyright 2016 Open mHealth
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
import org.openmhealth.shimmer.common.configuration.TokenPaginationSettings;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.TokenPaginationStatus;
import org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation;
import org.openmhealth.shimmer.common.domain.parameters.StringRequestParameter;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import org.testng.annotations.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation.PATH_VARIABLE;
import static org.openmhealth.shimmer.common.domain.parameters.RequestParameterLocation.QUERY_PARAMETER;


/**
 * @author Chris Schaefbauer
 */
public class TokenPaginationRequestEntityAssemblerUnitTests extends PaginationRequestEntityAssemblerUnitTests {

    @Test
    public void returnsBuilderWithCorrectQueryParameterWhenEndpointProvidesTokenInResponse() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://www.googleapis.com/fitness/v1/endpointInfo"));

        DefaultEndpointSettings configProperties = new DefaultEndpointSettings();

        TokenPaginationSettings tokenPaginationSettings = new TokenPaginationSettings();
        tokenPaginationSettings.setNextPageParameter(createNextPageTokenParameter(QUERY_PARAMETER));
        configProperties.setPaginationSettings(tokenPaginationSettings);

        PaginationStatus paginationStatus = new TokenPaginationStatus();
        paginationStatus.setPaginationResponseValue("1436566038006058105");

        TokenPaginationRequestEntityAssembler tokenAssembler =
                new TokenPaginationRequestEntityAssembler(tokenPaginationSettings);

        RequestEntityBuilder assembledBuilder =
                tokenAssembler.assemble(builder, createTestDataPointRequest(configProperties, paginationStatus));

        URI expectedUri = UriComponentsBuilder.fromUriString(
                "https://www.googleapis.com/fitness/v1/endpointInfo?pageToken=1436566038006058105")
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
    }

    @Test
    public void returnsBuilderWithCorrectPathParameterWhenEndpointProvidesTokenInResponse() {

        RequestEntityBuilder builder =
                new RequestEntityBuilder(
                        new UriTemplate("https://www.googleapis.com/fitness/v1/{pageToken}/endpointInfo"));

        DefaultEndpointSettings configProperties = new DefaultEndpointSettings();

        TokenPaginationSettings settings = new TokenPaginationSettings();
        settings.setNextPageParameter(createNextPageTokenParameter(PATH_VARIABLE));
        configProperties.setPaginationSettings(settings);

        PaginationStatus paginationStatus = new TokenPaginationStatus();
        paginationStatus.setPaginationResponseValue("1436566038006058105");

        TokenPaginationRequestEntityAssembler tokenAssembler =
                new TokenPaginationRequestEntityAssembler(settings);

        RequestEntityBuilder assembledBuilder =
                tokenAssembler.assemble(builder, createTestDataPointRequest(configProperties, paginationStatus));

        URI expectedUri = UriComponentsBuilder.fromUriString(
                "https://www.googleapis.com/fitness/v1/1436566038006058105/endpointInfo")
                .build().encode().toUri();

        assertThat(assembledBuilder.build().getUrl(), equalTo(expectedUri));
    }

    private StringRequestParameter createNextPageTokenParameter(RequestParameterLocation location) {

        StringRequestParameter nextPageTokenParameter = new StringRequestParameter();

        nextPageTokenParameter.setName("pageToken");
        nextPageTokenParameter.setLocation(location);

        return nextPageTokenParameter;
    }

}
