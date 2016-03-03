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
import org.openmhealth.shimmer.common.configuration.EndpointSettings;
import org.openmhealth.shimmer.common.domain.ProcessedResponse;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


/**
 * Implementation of the response processor.
 * @author Chris Schaefbauer
 */
public class ResponseProcessorImpl<T> implements ResponseProcessor<T> {

    @Override
    public ProcessedResponse processResponse(EndpointSettings settings,
            ResponseEntity<JsonNode> response) {

        ProcessedResponse processedResponse = new ProcessedResponse();

        HttpStatus statusCode = response.getStatusCode();

        /* The response processor could check status code information, log that information, and take action based on
         the status code in the response.
         */

        //        if(!statusCode.is2xxSuccessful()){
        //            //log issue
        //            return ProcessedResponse.Error(statusCode);
        //        }
        //
        //        processedResponse.setStatusCode(statusCode);


        /* The response processor could retrieve the appropriate mappers, based on the settings or from the shim
        registry, and then map that data if normalized data is requested
        */

        //        Shim targetShim;
        //= getShimByApiSourceName(endpointProperties.getApiSourceName());
        //        DataPointMapper mapper = targetShim.getMapperForSchema();
        //        List<DataPoint> dataPoints = mapper.asDataPoints(Collections.singletonList(responseEntity.getBody()));
        //
        //        processedResponse.setMappedData(dataPoints);
        //

        /* And finally process the pagination information contained in the response to determine whether more data is
         available and extract and necessary information from the response to successfully traverse the pagination
         upstream.
         */
        if (settings.supportsPaginationInResponses()) {
            PaginationResponseProcessor paginationProcessor;
            switch ( settings.getPaginationSettings().get().getResponseType() ) {
                case URI:
                case TOKEN:
                    paginationProcessor = new SimplePaginationResponseProcessor();
                    break;
                case MANUAL:
                    paginationProcessor = new ManualPaginationResponseProcessor();
                    break;
                //                case CUSTOM:
                //                    paginationProcessor = targetShim.getCustomPaginationResponseProcessor();
                default:
                    throw new UnsupportedOperationException();

            }

            PaginationStatus paginationStatus = paginationProcessor.processPaginationResponse(
                    settings.getPaginationSettings().get(),
                    response);

            processedResponse.setPaginationStatus(paginationStatus);
        }

        return processedResponse;
    }
}