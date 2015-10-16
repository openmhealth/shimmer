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
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.openmhealth.shimmer.common.configuration.UriPaginationResponseConfigurationProperties;
import org.openmhealth.shimmer.common.domain.ResponseLocation;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.UriPaginationStatus;
import org.openmhealth.shimmer.common.domain.pagination.UriResponsePaginationStrategy;
import org.openmhealth.shimmer.common.extractor.PaginationResponseExtractor;
import org.openmhealth.shimmer.common.extractor.PassthroughPaginationResponseExtractor;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationResponseProcessor
        implements PaginationResponseProcessor<UriPaginationResponseConfigurationProperties> {

    @Override
    public PaginationStatus processPaginationResponse(
            UriPaginationResponseConfigurationProperties paginationResponseProperties,
            ResponseEntity<JsonNode> responseEntity) {

        UriPaginationStatus paginationStatus = new UriPaginationStatus();
        //        UriResponsePaginationStrategy paginationResponseStrategy =
        //                (UriResponsePaginationStrategy) paginationResponseProperties.getPaginationResponseStrategy();

        // addresses both should we paginate and how
        if (paginationResponseProperties.getPaginationResponseLocation() == ResponseLocation.BODY) {
            //body
            String paginationNextUriPropertyName = paginationResponseProperties.getNextPaginationPropertyIdentifier();
            asOptionalString(responseEntity.getBody(), paginationNextUriPropertyName)
                    .ifPresent(nextUri -> paginationStatus.setNextUriStringFromResponse(nextUri));
        }
        else {
            List<String> pagingHeaders = responseEntity.getHeaders().getOrDefault(
                    paginationResponseProperties.getNextPaginationPropertyIdentifier(), Lists.newArrayList());
            if (!pagingHeaders.isEmpty()) {

                //String extractedUri = paginationResponseStrategy.getPaginationResponseExtractor().extractUri(join(pagingHeaders,
                // ","));

                paginationStatus.setNextUriStringFromResponse(Joiner.on(",").skipNulls().join(pagingHeaders));
            }
        }

        // now on to the rest of how we paginate
        paginationResponseProperties.getBaseUri().ifPresent(baseUri -> paginationStatus.setBaseUri(baseUri));

        UriResponsePaginationStrategy uriResponsePaginationStrategy =
                paginationStatus.createNewResponseStrategyForType(); // Could we use a builder here?
        uriResponsePaginationStrategy.setPaginationResponseExtractor(getPaginationResponseExtractor());


        paginationStatus.setResponseStrategy(uriResponsePaginationStrategy);

        return paginationStatus;
    }

    public PaginationResponseExtractor getPaginationResponseExtractor() {
        return new PassthroughPaginationResponseExtractor(); // This needs to get dependency injected somehow, maybe from Shim?
    }
}
