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
import org.openmhealth.shimmer.common.configuration.UriPaginationResponseConfigurationProperties;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.UriPaginationStatus;
import org.springframework.http.ResponseEntity;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationResponseProcessor
        extends PaginationResponseProcessor<UriPaginationResponseConfigurationProperties> {


    @Override
    public PaginationStatus processPaginationResponse(
            UriPaginationResponseConfigurationProperties paginationResponseProperties,
            ResponseEntity<JsonNode> responseEntity) {

        UriPaginationStatus paginationStatus = new UriPaginationStatus();

        String paginationNextUriPropertyName = paginationResponseProperties.getNextPaginationPropertyIdentifier();

        if (getPaginationResponseDecoder().isPresent()) {

            getPaginationResponseExtractor().extractPaginationResponse(responseEntity, paginationNextUriPropertyName)
                    .ifPresent(nextUri -> paginationStatus.setPaginationResponseValue(
                            getPaginationResponseDecoder().get().decodePaginationResponseValue(nextUri)));
        }
        else {

            getPaginationResponseExtractor().extractPaginationResponse(responseEntity, paginationNextUriPropertyName)
                    .ifPresent(nextUri -> paginationStatus.setPaginationResponseValue(nextUri));
        }


        // now on to the rest of how we paginate, though we may not even need this since it comes from the configs
        //paginationResponseProperties.getBaseUri().ifPresent(baseUri -> paginationStatus.setBaseUri(baseUri));

        return paginationStatus;
    }


}
