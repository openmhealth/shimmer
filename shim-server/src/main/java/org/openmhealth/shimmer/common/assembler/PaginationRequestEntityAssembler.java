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

import org.openmhealth.shimmer.common.configuration.EndpointConfigurationProperties;
import org.openmhealth.shimmer.common.configuration.PaginationSettings;
import org.openmhealth.shimmer.common.domain.DataPointRequest;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.openmhealth.shimmer.common.domain.parameters.NumberRequestParameter;

import java.util.Optional;


/**
 * @author Chris Schaefbauer
 */
public abstract class PaginationRequestEntityAssembler implements RequestEntityAssembler {

    public static final String ARBITRARILY_LARGE_LIMIT = "10000";
    private PaginationStatus paginationStatus;

    @Override
    public RequestEntityBuilder assemble(RequestEntityBuilder builder, DataPointRequest request) {

        EndpointConfigurationProperties endpoint = request.getEndpoint();

        if (endpoint.supportsPagination()) {

            PaginationSettings paginationSettings = endpoint.getPaginationSettings().get();

            if (getPaginationStatus().isPresent() && getPaginationStatus().get().hasMoreData()) {

                /*  If there is pagination status present, then we know there has to be pagination response configs
                since
                the configs are used to create a pagination status. */
                //                PaginationSettings paginationResponseConfiguration =
                //                        endpoint.getPaginationSettings().get();
                builder = assembleForResponseType(builder, paginationSettings, paginationStatus);

            }

            if (!builder.isFinishedAssembling()) {

                // Now set the limit parameter if we need to
                if (paginationSettings.hasPaginationLimitDefault()) {

                    NumberRequestParameter paginationLimitParameter =
                            paginationSettings.getPaginationLimitParameter().get();

                    String limitValueString = ARBITRARILY_LARGE_LIMIT; // Set the value to something arbitrarily large

                    if (paginationSettings.hasPaginationMaxLimit()) {

                        limitValueString =
                                Integer.toString(paginationLimitParameter.getMaximumValue().get().intValue());
                    }

                    builder.addParameterWithValue(paginationLimitParameter, limitValueString);
                }
            }
        }

        return builder;
    }

    protected abstract RequestEntityBuilder assembleForResponseType(RequestEntityBuilder builder,
            PaginationSettings paginationSettings, PaginationStatus paginationStatus);

    public Optional<PaginationStatus> getPaginationStatus() {
        return Optional.ofNullable(paginationStatus);
    }

    public void setPaginationStatus(PaginationStatus paginationStatus) {
        this.paginationStatus = paginationStatus;
    }
}
