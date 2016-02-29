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
import org.openmhealth.shimmer.common.configuration.UriPaginationSettings;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shimmer.common.domain.ResponseLocation.BODY;
import static org.springframework.http.HttpStatus.OK;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationResponseProcessorUnitTests extends ResponseProcessorUnitTests {

    @Test
    public void processResponseShouldExtractUriValueFromBodyResponseWhenPresentAndNotEncoded() {

        UriPaginationResponseProcessor processor = new UriPaginationResponseProcessor();

        JsonNode body = asJsonNode("org/openmhealth/shimmer/common/service/uri-pagination-body-response.json");

        ResponseEntity<JsonNode> response = new ResponseEntity<>(body, OK);

        UriPaginationSettings settings =  new UriPaginationSettings();
        settings.setResponseInformationEncoded(false);
        settings.setNextPagePropertyIdentifier("data.links.next");
        settings.setPaginationResponseLocation(BODY);

        PaginationStatus status = processor.processPaginationResponse(settings, response);

        assertThat(status.getPaginationResponseValue().isPresent(), equalTo(true));
        assertThat(status.getPaginationResponseValue().get(),
                equalTo("/nudge/api/v.1.1/users/VB0mNZWqiOUDWkkl72vgRQ/moves?page_token=1440353340&limit=3"));
        assertThat(status.hasMoreData(), equalTo(true));
    }
}
