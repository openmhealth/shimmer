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
import org.openmhealth.shimmer.common.decoder.PaginationResponseDecoder;
import org.openmhealth.shimmer.common.decoder.PassthroughPaginationResponseDecoder;
import org.openmhealth.shimmer.common.decoder.PercentEncodingPaginationResponseDecoder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.extractor.PaginationResponseExtractor;
import org.openmhealth.shimmer.common.extractor.SimpleBodyPaginationResponseExtractor;
import org.openmhealth.shimmer.common.extractor.SimpleHeaderPaginationResponseExtractor;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static java.util.Optional.empty;


/**
 * Processes a response from a third-party API to identify if more data points are available via pagination and to
 * extract the information necessary to traverse the pagination to retrieve those data points.
 *
 * @author Chris Schaefbauer
 */
public abstract class PaginationResponseProcessor<T extends PaginationSettings> {



    /*
    It doesn't make sense for the response processor to have an extractor and a decoder since, we shouldn't have a
    response processor per endpoint and in theory the decoder and extractor might be set at the settings level or
    chosen based on settings.
     */

    /**
     * Processes the pagination content in the response and loads the object to respond to requests for
     * information
     * about pagination.
     */
    public abstract PaginationStatus processPaginationResponse(T paginationResponseProperties,
            ResponseEntity<JsonNode> responseEntity);

    public PaginationResponseExtractor getPaginationResponseExtractor(PaginationSettings settings) {

        switch ( settings.getPaginationResponseLocation() ) {
            case BODY:
                return new SimpleBodyPaginationResponseExtractor();
            case HEADER:
                return new SimpleHeaderPaginationResponseExtractor();
            default:
                // throw exception
        }

        return null;
    }

    /**
     * Used to decode pagination response information that is encoded in some form.
     *
     * @return The decoder to use in processing the pagination response information, if one exists.
     */
    public Optional<PaginationResponseDecoder> getPaginationResponseDecoder(PaginationSettings settings) {

        if (!settings.getResponseEncodingType().isPresent()) {
            return empty();
        }

        switch ( settings.getResponseEncodingType().get() ) {
            case NONE:
                return Optional.of(new PassthroughPaginationResponseDecoder());
            case PERCENT_ENCODING:
                return Optional.of(new PercentEncodingPaginationResponseDecoder());
            default:
                //throw configuration exception
        }
        return empty();
    }

}

