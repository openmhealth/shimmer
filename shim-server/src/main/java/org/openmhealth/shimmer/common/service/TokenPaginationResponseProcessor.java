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
import org.openmhealth.shimmer.common.domain.pagination.TokenPaginationStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * @author Chris Schaefbauer
 */
public class TokenPaginationResponseProcessor extends PaginationResponseProcessor {

    @Override
    public PaginationStatus processPaginationResponse(PaginationSettings settings,
            ResponseEntity<JsonNode> response) {

        TokenPaginationStatus paginationStatus = new TokenPaginationStatus();

        String nextPaginationPropertyIdentifier = settings.getNextPagePropertyIdentifier().get();

        switch ( settings.getPaginationResponseLocation() ) {

            case BODY:
                Optional<JsonNode> paginationValueNode =
                        asOptionalNode(response.getBody(), nextPaginationPropertyIdentifier);
                if (paginationValueNode.isPresent()) {

                    if (paginationValueNode.get().isTextual()) {
                        paginationStatus.setPaginationResponseValue(
                                asRequiredString(response.getBody(), nextPaginationPropertyIdentifier));
                    }
                    else if (paginationValueNode.get().isInt()) {
                        paginationStatus.setPaginationResponseValue(
                                asRequiredInteger(response.getBody(), nextPaginationPropertyIdentifier).toString());
                    }
                    else {
                        //throw incompatible type exception
                    }
                }
                break;

            case HEADER:
                String headerValue = response.getHeaders().getFirst(nextPaginationPropertyIdentifier);

                if (headerValue != null) {
                    paginationStatus.setPaginationResponseValue(headerValue);
                }
                break;
        }

        return paginationStatus;
    }
}
