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
import org.openmhealth.shimmer.common.configuration.UriPaginationSettings;
import org.openmhealth.shimmer.common.domain.ResponseLocation;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shimmer.common.domain.ResponseLocation.BODY;
import static org.openmhealth.shimmer.common.domain.ResponseLocation.HEADER;
import static org.openmhealth.shimmer.common.domain.pagination.PaginationResponseEncoding.PERCENT_ENCODING;
import static org.springframework.http.HttpStatus.OK;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationResponseProcessorUnitTests extends ResponseProcessorUnitTests {


    private UriPaginationResponseProcessor processor = new UriPaginationResponseProcessor();
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void processResponseShouldExtractUriValueFromBodyResponseWhenPresentAndNotEncoded() throws IOException {

        JsonNode responseNode = mapper.readTree("{\n" +
                "    \"a\":{\n" +
                "        \"b\" : {\n" +
                "            \"c\":\"http://tokenuri.com\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        ResponseEntity<JsonNode> responseWithUriInBody = new ResponseEntity<>(responseNode, OK);

        PaginationStatus status = processor.processPaginationResponse(
                getSettingsForLocationWithValues(BODY, "a.b.c", false), responseWithUriInBody);

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(),
                equalTo("http://tokenuri.com"));
        assertThat(status.hasMoreData(), equalTo(true));
    }

    @Test
    public void processResponseShouldIndicateNoMoreDataWhenUriValueIsNotPresentInBodyResponse() throws IOException {

        JsonNode responseNode = mapper.readTree("{\n" +
                "    \"a\":{}\n" +
                "}");

        ResponseEntity<JsonNode> responseWithNoUriInBody = new ResponseEntity<>(responseNode, OK);

        PaginationStatus status =
                processor.processPaginationResponse(getSettingsForLocationWithValues(BODY, "a.b.c", false),
                        responseWithNoUriInBody);

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(false));
        assertThat(status.hasMoreData(), equalTo(false));
    }

    @Test
    public void processResponseShouldExtractAndDecodeUriValueFromBodyResponseWhenPresentAndEncoded()
            throws IOException {

        JsonNode responseNode = mapper.readTree("{\n" +
                "    \"a\":{\n" +
                "        \"b\" : {\n" +
                "            \"c\":\"http%3A%2F%2Ftokenuri.com%3Ftoken%3D123%26limit%3D4\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        ResponseEntity<JsonNode> responseWithUriInBodyWithEncoding = new ResponseEntity<>(responseNode, OK);

        UriPaginationSettings settings = getSettingsForLocationWithValues(BODY, "a.b.c", true);
        settings.setPaginationResponseEncoding(PERCENT_ENCODING);

        PaginationStatus status = processor.processPaginationResponse(settings, responseWithUriInBodyWithEncoding);

        assertThat(status.hasMoreData(), equalTo(true));

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(), equalTo("http://tokenuri.com?token=123&limit=4"));
    }

    @Test
    public void processResponseShouldExtractUriValueFromHeaderResponseWhenPresent() {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        headers.add("NextPage", "http://tokenuri.com");

        ResponseEntity<JsonNode> responseWithUriInHeader = new ResponseEntity<>(headers, OK);

        PaginationStatus status = processor
                .processPaginationResponse(getSettingsForLocationWithValues(HEADER, "NextPage", false),
                        responseWithUriInHeader);

        assertThat(status.hasMoreData(), equalTo(true));

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(), equalTo("http://tokenuri.com"));
    }

    @Test
    public void processResponseShouldReturnNoMoreDataWhenUriValueFromHeaderResponseIsNotPresent() {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        ResponseEntity<JsonNode> responseWithUriInHeader = new ResponseEntity<>(headers, OK);

        PaginationStatus status = processor
                .processPaginationResponse(getSettingsForLocationWithValues(HEADER, "NextPage", false),
                        responseWithUriInHeader);

        assertThat(status.hasMoreData(), equalTo(false));
        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(false));
    }

    @Test
    public void processResponseShouldExtractUriValueFromBodyResponseForJawbone() {

        ResponseEntity<JsonNode> responseWithUriInBody = new ResponseEntity<>(
                asJsonNode("org/openmhealth/shimmer/common/service/jawbone-uri-pagination-body-response.json"), OK);


        PaginationStatus status =
                processor.processPaginationResponse(getSettingsForLocationWithValues(BODY, "data.links.next", false),
                        responseWithUriInBody);

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(),
                equalTo("/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token=1440353340&limit=3"));
        assertThat(status.hasMoreData(), equalTo(true));
    }

    @Test
    public void processResponseShouldIndicateNoMoreDataWhenUriValueIsNotPresentInBodyResponseForJawbone() {

        ResponseEntity<JsonNode> responseWithNoUriInBody = new ResponseEntity<>(
                asJsonNode("org/openmhealth/shimmer/common/service/jawbone-uri-pagination-no-body-response.json"), OK);

        PaginationStatus status =
                processor.processPaginationResponse(getSettingsForLocationWithValues(BODY, "data.links.next", false),
                        responseWithNoUriInBody);

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(false));
        assertThat(status.hasMoreData(), equalTo(false));
    }

    @Test
    public void processResponseShouldExtractAndDecodeUriValueFromBodyResponseWhenPresentAndEncodedForIHealth() {

        ResponseEntity<JsonNode> responseWithUriInBodyWithEncoding = new ResponseEntity<>(
                asJsonNode(
                        "org/openmhealth/shimmer/common/service/ihealth-uri-pagination-body-response-with-encoding" +
                                ".json"),
                OK);

        UriPaginationSettings settings = getSettingsForLocationWithValues(BODY, "NextPageUrl", true);
        settings.setPaginationResponseEncoding(PERCENT_ENCODING);

        PaginationStatus status = processor.processPaginationResponse(settings, responseWithUriInBodyWithEncoding);

        assertThat(status.hasMoreData(), equalTo(true));

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(),
                equalTo("https://api.ihealthlabs.com:8443/openapiv2/user/758d6e5a23c9495bad5c43bab40b7e3c/activity" +
                        ".json/?client_id=b6a4d7971a894ad9897689088ae0bdad&client_secret" +
                        "=1a319d362e35431cbd4d855f33a1026d&redirect_uri=http%3a%2f%2fsafedev.openmhealth" +
                        ".org&access_token=EyiqrRKEuqDHl6PQRBrRsE0a2IsUTUtvq21s87r4GOGhgj9fzxOb9I3CP-z*aPpGy9qwUinSa" +
                        "*IIMHWeHB*5zkgS2tokptnzNvDuI2FTfZzor-ajL*MW4DeUBAeVgSYS6F6zfClNnQbZ0g4auLct4Q" +
                        "*baMbbXeBroukKaPf1Rjip06lfadXN*7W45jWu1CIDrPZzsC6JaH-Ix3oRasLEpg&sc" +
                        "=d3fd64b0ba8e4fbaa4d0b8a65c68d46e&sv=c2d2e3ffde224d1fb7a8f4a367a2ba90&page_index=2"));
    }

    public UriPaginationSettings getSettingsForLocationWithValues(ResponseLocation location, String nextPagePropertyId,
            boolean isEncoded) {

        UriPaginationSettings settings = new UriPaginationSettings();

        settings.setResponseInformationEncoded(isEncoded);
        settings.setNextPagePropertyIdentifier(nextPagePropertyId);
        settings.setPaginationResponseLocation(location);

        return settings;
    }
}
