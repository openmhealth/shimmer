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

package org.openmhealth.shimmer.common.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

import java.util.Optional;


/**
 * Extracts the information necessary to traverse pagination from a third-party API response. By extracting the
 * extraction of this information into a separate component, we enable other developers to plug-in their own extractor
 * to handle more complicated APIs in the future.
 *
 * @author Chris Schaefbauer
 */
public interface PaginationResponseExtractor {

    /**
     * @param responseEntity The response from which pagination information should be extracted.
     * @param paginationPropertyIdentifier The path to the value that enables pagination traversal. For example, for
     * URI-based pagination responses, this would be the path to the value containing the URI fragment or URI that
     * references the next page of data points.
     * @return The next page traversal value extracted from the response, if one is present in the response.
     */
    Optional<String> extractPaginationResponse(ResponseEntity<JsonNode> responseEntity,
            String paginationPropertyIdentifier);
}
