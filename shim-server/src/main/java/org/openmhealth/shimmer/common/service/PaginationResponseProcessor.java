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

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.shimmer.common.configuration.PaginationSettings;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.springframework.http.ResponseEntity;


/**
 * Processes a response from a third-party API to identify if more data points are available via pagination and to
 * extract the information necessary to traverse the pagination to retrieve those data points.
 *
 * @author Chris Schaefbauer
 */
public interface PaginationResponseProcessor {

    /*
    It doesn't make sense for the response processor to have an extractor and a decoder since, we shouldn't have a
    response processor per endpoint and in theory the decoder and extractor might be set at the settings level or
    chosen based on settings.
     */

    /**
     * Processes the pagination content in the response and loads the object to respond to requests for
     * information
     * about pagination.
     */
    PaginationStatus processPaginationResponse(PaginationSettings paginationSettings,
            ResponseEntity<JsonNode> responseEntity);

}

