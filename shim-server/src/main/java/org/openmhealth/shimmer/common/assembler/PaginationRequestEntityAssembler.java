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

package org.openmhealth.shimmer.common.assembler;

import org.openmhealth.shimmer.common.configuration.PaginationSettings;
import org.openmhealth.shimmer.common.domain.DataPointRequest;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.domain.parameters.NumberRequestParameter;


/**
 * Adds the appropriate pagination parameters to a request entity builder that is being assembled. The pagination
 * information added to the builder is based on the API's pagination settings and pagination status information from
 * the the response to the previous request, if one has already been made.
 *
 * @author Chris Schaefbauer
 */
public abstract class PaginationRequestEntityAssembler implements RequestEntityAssembler {

    // In the case where an API has a default value for the limit of data points that can be requested, but no
    // maximum limit, this value should be used to override the default limit, so we have fewer pages of data to handle.
    public static final String ARBITRARILY_LARGE_LIMIT = "10000";

    @Override
    public RequestEntityBuilder assemble(RequestEntityBuilder builder, DataPointRequest request) {

        // The children implement a getPaginationSettings object with the correct type, the configuration is
        // passed in with the right type
        PaginationSettings paginationSettings = getPaginationSettings();


        // If there is the pagination status is present and there is more data, then we need to assemble the
        // pagination parameters according to the settings and status information.
        if (request.getPaginationStatus().isPresent() && request.getPaginationStatus().get().hasMoreData()) {

                /*  If there is pagination status present, then we know there has to be pagination response configs
                since the configs are used to create a pagination status. */
            //                PaginationSettings paginationResponseConfiguration =
            //                        endpoint.getPaginationSettings().get();
            builder = assembleForResponseType(builder, request.getPaginationStatus().get());

        }

        // Previously, if the API provided sufficient information in the response to construct the entirety of the next response,
        // then we may not need to continue assembling. But it might not hurt to keep assembling.


        // We need to set the limit if the API has a default limit value, since we will want to override it to
        // increase the value to reduce pagination.
        if (paginationSettings.hasPaginationLimitDefault()) {

            NumberRequestParameter paginationLimitParameter =
                    paginationSettings.getPaginationLimitParameter().get();

            String limitValueString = ARBITRARILY_LARGE_LIMIT; // Set the value to something arbitrarily large

            // If the API specifies a maximum on the number of data points we can request, then we should use that
            // maximum value instead of an arbitrary one, which it would likely reject.
            if (paginationSettings.hasPaginationMaxLimit()) {

                limitValueString =
                        Integer.toString(paginationLimitParameter.getMaximumValue().get().intValue());
            }

            builder.addParameterWithValue(paginationLimitParameter, limitValueString);
        }


        return builder;
    }

    /**
     * Adds pagination information specific to a given type of pagination response. Implemented by pagination response
     * specific subclasses.
     *
     * @param builder The request entity builder to which pagination parameter information should be added.
     * @param paginationStatus The status of pagination for the current request based on the prior response. If this is
     * the first request, then pagination status will indicate that.
     * @return The RequestEntityBuilder containing the appropriate pagination parameters.
     */
    protected abstract RequestEntityBuilder assembleForResponseType(RequestEntityBuilder builder,
            PaginationStatus paginationStatus);

    /**
     * @return The pagination settings for the API, so they can be used in assembling the RequestEntityBuilder with
     * pagination parameter information.
     */
    public abstract PaginationSettings getPaginationSettings();
}
