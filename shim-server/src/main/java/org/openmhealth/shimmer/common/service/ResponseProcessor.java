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
import org.openmhealth.shimmer.common.configuration.EndpointSettings;
import org.openmhealth.shimmer.common.domain.ProcessedResponse;
import org.springframework.http.ResponseEntity;


/**
 * Processes a response from a third-party API to handle the status of the response, extract data from it, and
 * determine if there is further data available via pagination.
 *
 * @author Chris Schaefbauer
 */
public interface ResponseProcessor<T> {

    /**
     * @param settings The settings for the endpoint from which the response was retrieved.
     * @param response The response returned by the third-party API.
     * @return A processed response containing status information, data, and pagination status information necessary to
     * retrieve additional data points if they exist.
     */
    ProcessedResponse processResponse(EndpointSettings settings,
            ResponseEntity<JsonNode> response);

}
