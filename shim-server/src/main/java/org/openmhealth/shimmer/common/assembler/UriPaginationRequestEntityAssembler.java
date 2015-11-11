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
import org.openmhealth.shimmer.common.configuration.UriPaginationSettings;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;
import org.springframework.web.util.UriTemplate;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationRequestEntityAssembler extends PaginationRequestEntityAssembler {

    @Override
    protected RequestEntityBuilder assembleForResponseType(RequestEntityBuilder builder,
            PaginationSettings paginationSettings, PaginationStatus paginationStatus) {

        UriPaginationSettings uriPaginationResponseConfiguration =
                (UriPaginationSettings) paginationSettings;

        if (uriPaginationResponseConfiguration.providesCompleteUri()) {

            builder.setUriTemplate(
                    new UriTemplate(paginationStatus.getPaginationResponseValue().get()));
            builder.setFinishedAssembling(true);
        }
        else {

            builder.setUriTemplate(
                    new UriTemplate(uriPaginationResponseConfiguration.getBaseUri().get()));
            builder.addPathParameter("paginationResponse",
                    paginationStatus.getPaginationResponseValue().get());
        }

        return builder;
    }
}
