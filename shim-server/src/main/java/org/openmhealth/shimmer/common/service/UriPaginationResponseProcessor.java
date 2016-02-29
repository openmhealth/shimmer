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
import org.openmhealth.shimmer.common.configuration.UriPaginationSettings;
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
public class UriPaginationResponseProcessor extends PaginationResponseProcessor<UriPaginationSettings> {

    @Override
    public PaginationStatus processPaginationResponse(UriPaginationSettings settings,
            ResponseEntity<JsonNode> response) {

        UriPaginationStatus paginationStatus = new UriPaginationStatus();

        String paginationNextUriPropertyName = settings.getNextPagePropertyIdentifier();

        switch (settings.getPaginationResponseLocation()) {

            case HEADER:
                String headerValue = response.getHeaders().getFirst(paginationNextUriPropertyName);

                if (headerValue != null) {
                    paginationStatus.setPaginationResponseValue(headerValue);
                }
                break;

            case BODY:
                Optional<String> responseValue = asOptionalString(response.getBody(), paginationNextUriPropertyName);

                if(responseValue.isPresent()){

                    if (settings.isResponseInformationEncoded()) {

                        try {

                            paginationStatus.setPaginationResponseValue(decode(responseValue.get(), "UTF-8"));
                        }
                        catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            // todo: Handle the exception
                        }
                    }
                    else{

                        paginationStatus.setPaginationResponseValue(responseValue.get());
                    }
                }
        }
        //        else {
//
//            getPaginationResponseExtractor(settings).extractPaginationResponse(response, paginationNextUriPropertyName)
//                    .ifPresent(nextUri -> paginationStatus.setPaginationResponseValue(nextUri));
//        }


        // now on to the rest of how we paginate, though we may not even need this since it comes from the configs
        // paginationResponseProperties.getBaseUri().ifPresent(baseUri -> paginationStatus.setBaseUri(baseUri));

        return paginationStatus;
    }


}
