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
import org.openmhealth.shimmer.common.configuration.DateTimeQuerySettings;
import org.openmhealth.shimmer.common.domain.DataPointRequest;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;
import org.openmhealth.shimmer.common.domain.parameters.DateTimeRequestParameter;

import java.time.OffsetDateTime;


/**
 * Adds the appropriate date/time parameter information to RequestEntityBuilders that are being assembled. The
 * date/time information added is based on the settings of the API for which the request is being made as well as the
 * time frame being requested.
 *
 * @author Chris Schaefbauer
 */
public class DateTimeRequestEntityAssembler implements RequestEntityAssembler {

    private final Range<OffsetDateTime> timeRange;
    private DateTimeQuerySettings querySettings;

    /**
     *
     * @param querySettings The date/time request settings for the API for which data is being requested.
     * @param timeRange The time range for which data is being requested.
     */
    public DateTimeRequestEntityAssembler(DateTimeQuerySettings querySettings, Range<OffsetDateTime> timeRange) {

        this.querySettings = querySettings;
        this.timeRange = timeRange;
    }

    @Override
    public RequestEntityBuilder assemble(RequestEntityBuilder builder, DataPointRequest request) {

        if (querySettings.getDateTimeParameter().isPresent()) {

            OffsetDateTime valueForRequest = OffsetDateTime.now();

            if (!timeRange.hasLowerBound()) {

                valueForRequest = timeRange.upperEndpoint();
            }
            else if (!timeRange.hasUpperBound()) {

                valueForRequest = timeRange.lowerEndpoint();
            }
            else if (timeRange.lowerEndpoint().isEqual(timeRange.upperEndpoint())) {

                valueForRequest = timeRange.lowerEndpoint();
            }
            // Todo: Consider making the upper bound on single days (begin of day -> end of day) to be same localdate
            else if (timeRange.lowerEndpoint().toLocalDate().isEqual(timeRange.upperEndpoint().toLocalDate()) ||
                    timeRange.lowerEndpoint().toLocalDate().isEqual(
                            timeRange.upperEndpoint().minusMinutes(1).toLocalDate())) {

                valueForRequest = timeRange.lowerEndpoint();
            }
            else {
                // Todo: throw invalid time range exception
            }

            DateTimeRequestParameter dateTimeParameter = querySettings.getDateTimeParameter().get();
            builder.addParameterWithValue(dateTimeParameter,
                    dateTimeParameter.getDateTimeFormat().translate(valueForRequest));

        }
        else {

            if (timeRange.hasLowerBound()) {

                if (!querySettings.getStartDateTimeParameter().isPresent()) {
                    // Todo: throw configuration exception
                }

                DateTimeRequestParameter startRequestParameter =
                        querySettings.getStartDateTimeParameter().get();

                builder.addParameterWithValue(startRequestParameter,
                        startRequestParameter.getDateTimeFormat().translate(timeRange.lowerEndpoint()));
            }

            if (timeRange.hasUpperBound()) {

                if (!querySettings.getEndDateTimeParameter().isPresent()) {
                    // Todo: throw configuration exception
                }

                DateTimeRequestParameter endRequestParameter =
                        querySettings.getEndDateTimeParameter().get();

                builder.addParameterWithValue(endRequestParameter,
                        endRequestParameter.getDateTimeFormat().translate(timeRange.lowerEndpoint()));
            }
        }

        return builder;
    }

}
