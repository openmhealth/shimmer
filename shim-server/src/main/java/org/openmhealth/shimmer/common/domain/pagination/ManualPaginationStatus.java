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
 * @author Chris Schaefbauer
 */
public class ManualPaginationStatus implements PaginationStatus {

    private String endPropertyIdentifier;

    // Todo: may want the hasMoreData to take a ResponseConfiguration

    private Integer nextPageOffset;

    private ManualPaginationEndCriteria endCriteria;
    private JsonNode responseBody;


    public ManualPaginationStatus(JsonNode responseBody, ManualPaginationEndCriteria endCriteria,
            String paginationEndPropertyIdentifier) {
        this.responseBody = responseBody;
        this.endCriteria = endCriteria;
        this.endPropertyIdentifier = paginationEndPropertyIdentifier;
    }


    @Override
    public boolean hasMoreData() {

        Optional<JsonNode> endPropertyNodeOptional = asOptionalNode(responseBody, endPropertyIdentifier);
        switch ( endCriteria ) {
            case EMPTY_RESPONSE:
                if (!responseBody.elements().hasNext()) {
                    return false;
                }
                return true;
            case EMPTY_OR_MISSING_FIELD:

                if(!endPropertyNodeOptional.isPresent()){
                    return false;
                }
                JsonNode endPropertyNode = endPropertyNodeOptional.get();
                if(endPropertyNode.isArray()){
                    if(endPropertyNode.size() == 0){
                        return false;
                    }
                }
                if(endPropertyNode.isTextual()){
                    if(endPropertyNode.asText("").isEmpty()){
                        return false;
                    }
                }
                break;
            case EXPLICITLY_INDICATED:
                if(endPropertyNodeOptional.isPresent()){
                    endPropertyNodeOptional.get();
                }
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
