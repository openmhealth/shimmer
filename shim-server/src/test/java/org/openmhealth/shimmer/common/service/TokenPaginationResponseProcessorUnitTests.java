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

package org.openmhealth.shimmer.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmhealth.shimmer.common.configuration.TokenPaginationSettings;
import org.openmhealth.shimmer.common.domain.ResponseLocation;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shimmer.common.domain.ResponseLocation.BODY;
import static org.springframework.http.HttpStatus.OK;


/**
 * @author Chris Schaefbauer
 */
public class TokenPaginationResponseProcessorUnitTests extends ResponseProcessorUnitTests {

    private TokenPaginationResponseProcessor processor = new TokenPaginationResponseProcessor();
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void processResponseShouldReturnTokenFromBodyResponseWhenPresent() throws IOException {

        JsonNode responseNode = mapper.readTree("{\n" +
                "    \"a\":{\n" +
                "        \"b\" : {\n" +
                "            \"c\":\"TOKEN123\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        ResponseEntity<JsonNode> responseWithTokenInBody = new ResponseEntity<>(responseNode, OK);

        PaginationStatus status = processor
                .processPaginationResponse(getSettingsForLocationWithValues(BODY, "a.b.c", false),
                        responseWithTokenInBody);

        assertThat(status.hasMoreData(), equalTo(true));

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(), equalTo("TOKEN123"));
    }

    @Test
    public void processResponseShouldIndicateNoMoreDataWhenTokenValueIsNotPresentInBodyResponse() throws IOException {

        JsonNode responseNode = mapper.readTree("{\n" +
                "    \"a\":{}\n" +
                "}");

        ResponseEntity<JsonNode> responseWithNoTokenInBody = new ResponseEntity<>(responseNode, OK);

        PaginationStatus status = processor
                .processPaginationResponse(getSettingsForLocationWithValues(BODY, "a.b.c", false),
                        responseWithNoTokenInBody);

        assertThat(status.hasMoreData(), equalTo(false));
        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(false));
    }

    @Test
    public void processResponseShouldReturnTokenFromBodyResponseWhenPresentForGoogle() {

        ResponseEntity<JsonNode> responseWithTokenInBody = new ResponseEntity<>(
                asJsonNode("org/openmhealth/shimmer/common/service/google-token-pagination-body-response.json"), OK);

        PaginationStatus status = processor
                .processPaginationResponse(getSettingsForLocationWithValues(BODY, "nextPageToken", false),
                        responseWithTokenInBody);

        assertThat(status.hasMoreData(), equalTo(true));

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(), equalTo("1436566038006058105"));

    }

    public TokenPaginationSettings getSettingsForLocationWithValues(ResponseLocation location,
            String nextPagePropertyId, boolean isEncoded) {

        TokenPaginationSettings settings = new TokenPaginationSettings();

        settings.setResponseInformationEncoded(isEncoded);
        settings.setNextPagePropertyIdentifier(nextPagePropertyId);
        settings.setPaginationResponseLocation(location);

        return settings;
    }


}
