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
import org.openmhealth.shimmer.common.configuration.TokenPaginationSettings;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;


/**
 * @author Chris Schaefbauer
 */
public class TokenPaginationRequestEntityAssembler extends PaginationRequestEntityAssembler {

    private TokenPaginationSettings paginationSettings;

    public TokenPaginationRequestEntityAssembler(TokenPaginationSettings paginationSettings) {

        this.paginationSettings = paginationSettings;
    }

    @Override
    protected RequestEntityBuilder assembleForResponseType(RequestEntityBuilder builder,
            PaginationStatus paginationStatus) {


        if (paginationSettings.getNextPageParameter().isPresent()) {

            builder.addParameterWithValue(paginationSettings.getNextPageParameter().get(),
                    paginationStatus.getPaginationResponseValue().get());

        }
        else {
            // Todo: Throw a pagination configuration missing exception
        }
        return builder;
    }

    @Override
    public PaginationSettings getPaginationSettings() {
        return paginationSettings;
    }
}
