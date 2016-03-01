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
import org.openmhealth.shimmer.common.domain.pagination.UriPaginationStatus;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static java.net.URLDecoder.decode;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationResponseProcessor extends PaginationResponseProcessor {

    @Override
    public PaginationStatus processPaginationResponse(PaginationSettings settings,
            ResponseEntity<JsonNode> response) {

        UriPaginationStatus paginationStatus = new UriPaginationStatus();

        String paginationNextUriPropertyName = settings.getNextPagePropertyIdentifier().get();

        Optional<String> nextPaginationValue = Optional.empty();

        switch (settings.getPaginationResponseLocation()) {

            case HEADER:
                nextPaginationValue = Optional.ofNullable(response.getHeaders().getFirst(paginationNextUriPropertyName));
                break;

            case BODY:
                // Assuming that we have an optional String parser that can handle numeric values
                nextPaginationValue = asOptionalString(response.getBody(), paginationNextUriPropertyName);
                break;
        }

        if (!nextPaginationValue.isPresent()) {
            return paginationStatus;
        }

        // May need to decode values that come from the Header or Body
        if (settings.isResponseInformationEncoded()) {

            try {
                paginationStatus.setPaginationResponseValue(decode(nextPaginationValue.get(), "UTF-8"));
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // todo: Handle the exception
            }
        }
        else{
            paginationStatus.setPaginationResponseValue(nextPaginationValue.get());
        }

        return paginationStatus;
    }


}
