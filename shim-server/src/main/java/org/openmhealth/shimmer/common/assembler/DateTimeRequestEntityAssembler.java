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

import com.google.common.collect.Range;
import org.openmhealth.shimmer.common.configuration.DateTimeQueryConfigurationProperties;
import org.openmhealth.shimmer.common.configuration.EndpointConfigurationProperties;
import org.openmhealth.shimmer.common.domain.DataPointRequest;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.parameters.DateTimeRequestParameter;
import org.openmhealth.shimmer.common.translator.DateTimeFormatTranslator;

import java.time.OffsetDateTime;


/**
 * @author Chris Schaefbauer
 */
public abstract class DateTimeRequestEntityAssembler implements RequestEntityAssembler {

    @Override
    public RequestEntityBuilder assemble(RequestEntityBuilder builder, DataPointRequest request) {

        EndpointConfigurationProperties endpoint = request.getEndpoint();

        if (request.getEffectiveTimestampRange().isPresent()) {

            Range<OffsetDateTime> effectiveTimeRange = request.getEffectiveTimestampRange().get();


            if (!endpoint.getEffectiveDateTimeQuerySettings().isPresent()) {
                // Todo: Throw configuration error
            }

            DateTimeQueryConfigurationProperties effectiveTimeFrameConfigurationProperties =
                    endpoint.getEffectiveDateTimeQuerySettings().get();

            if (effectiveTimeRange.hasLowerBound()) {


                if (!effectiveTimeFrameConfigurationProperties.getStartDateTimeParameter().isPresent()) {
                    // Todo: throw configuration error
                }

                DateTimeRequestParameter startRequestParameter =
                        effectiveTimeFrameConfigurationProperties.getStartDateTimeParameter().get();

                DateTimeFormatTranslator formatTranslator = startRequestParameter.getDateTimeFormat();

                builder.addParameterWithValue(startRequestParameter, formatTranslator.translate(
                        effectiveTimeRange.lowerEndpoint()));
            }

            if (effectiveTimeFrameConfigurationProperties.getEndDateTimeParameter().isPresent()) {

                // Todo: implement end effective time frame

            }


        }

        if (request.getCreationTimestampRange().isPresent()) {

            //Todo: implement creation time frame assembling

            if (!endpoint.getCreationDateTimeQuerySettings().isPresent()) {
                // Todo: Throw configuration error
            }

        }

        return builder;
    }

}
