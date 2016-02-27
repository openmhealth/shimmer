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

package org.openmhealth.shimmer.common.domain.pagination;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalNode;


/**
 * Encapsulates information necessary to successfully request the next page of data points for an API that provides no
 * information in the response as a means of pagination in their responses. Instead APIs that use manual pagination
 * provide the necessary information to paginate a-priori, however the end state for pagination (i.e., when all data
 * points have been retrieved) must be assessed given pagination configuration settings and the response.
 *
 * @author Chris Schaefbauer
 */
public class ManualPaginationStatus implements PaginationStatus {

    private String endPropertyIdentifier;

    // Todo: may want the hasMoreData to take a ResponseConfiguration

    private Integer nextPageOffset;

    private ManualPaginationEndCriteria endCriteria;

    // We may need the entire response body to test whether we have reached the end of pagination.
    private JsonNode responseBody;


    public ManualPaginationStatus(JsonNode responseBody, ManualPaginationEndCriteria endCriteria,
            String paginationEndPropertyIdentifier) {

        this.responseBody = responseBody;
        this.endCriteria = endCriteria;
        this.endPropertyIdentifier = paginationEndPropertyIdentifier;
    }


    /**
     * Processes the response to determine whether there is more data available depending on the settings of the API.
     * Maybe this processing should be done in a setter or in another method?
     *
     * @return TRUE if more data points are available through pagination, FALSE otherwise
     */
    @Override
    public boolean hasMoreData() {

        // Assuming there is a property whose presence, absence, or specific value indicates the end of the
        // pagination, then we should attempt to get that property.
        Optional<JsonNode> endPropertyNodeOptional = asOptionalNode(responseBody, endPropertyIdentifier);

        // Depending on how the API indicates that we should stop requesting more pages of data, we will check
        // whether the appropriate conditions have been satisfied to indicate there are no more pages of data. There
        // are certainly additional methods by which this can be indicated that are not addressed here, such as
        // headers or HTTP responses, that need to be added.
        switch ( endCriteria ) {
            // This is the case where an API provides an empty response to indicate that there are no more data points
            // available.
            case EMPTY_RESPONSE:
                if (!responseBody.elements().hasNext()) {
                    return false;
                }
                return true;

            // This is the case where a specific field or value is missing or empty to indicate that there are no
            // more data points available.
            case EMPTY_OR_MISSING_FIELD:

                if (!endPropertyNodeOptional.isPresent()) {
                    return false;
                }
                JsonNode endPropertyNode = endPropertyNodeOptional.get();
                if (endPropertyNode.isArray()) {
                    if (endPropertyNode.size() == 0) {
                        return false;
                    }
                }
                if (endPropertyNode.isTextual()) {
                    if (endPropertyNode.asText("").isEmpty()) {
                        return false;
                    }
                }
                break;

            // This is the case where an API provides an explicit value in the response to indicate that there are no
            // more data points available.
            case EXPLICITLY_INDICATED:
                if (endPropertyNodeOptional.isPresent()) {
                    endPropertyNodeOptional.get();
                }
                break;
        }
        return false;

    }

    //    public ManualPaginationEndCriteria getEndCriteria() {
    //        return endCriteria;
    //    }
    //
    //    public void setEndCriteria(ManualPaginationEndCriteria endCriteria) {
    //        this.endCriteria = endCriteria;
    //    }

    // Todo: determine how the next page value for manual paging can be tracked/retrieved
    @Override
    public Optional<String> getPaginationResponseValue() {
        return Optional.ofNullable(nextPageOffset.toString());
    }

    @Override
    public void setPaginationResponseValue(String nextPageOffsetValue) {
        nextPageOffset = Integer.parseInt(nextPageOffsetValue);
    }
}
