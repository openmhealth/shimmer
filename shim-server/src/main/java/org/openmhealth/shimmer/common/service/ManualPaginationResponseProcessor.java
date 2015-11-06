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
import org.openmhealth.shimmer.common.configuration.ManualPaginationResponseConfigurationProperties;
import org.openmhealth.shimmer.common.domain.pagination.ManualPaginationEndCriteria;
import org.openmhealth.shimmer.common.domain.pagination.ManualPaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.springframework.http.ResponseEntity;


/**
 * @author Chris Schaefbauer
 */
public class ManualPaginationResponseProcessor
        extends PaginationResponseProcessor<ManualPaginationResponseConfigurationProperties> {

    @Override
    public PaginationStatus processPaginationResponse(
            ManualPaginationResponseConfigurationProperties paginationResponseProperties,
            ResponseEntity<JsonNode> responseEntity) {

        ManualPaginationEndCriteria paginationEndCriteria = paginationResponseProperties.getPaginationEndCriteria();
        String endPaginationPropertyIdentifier = paginationResponseProperties.getEndPaginationPropertyIdentifier();

        ManualPaginationStatus paginationStatus =
                new ManualPaginationStatus(responseEntity.getBody(), paginationEndCriteria,
                        endPaginationPropertyIdentifier);

        return paginationStatus;
    }
}
