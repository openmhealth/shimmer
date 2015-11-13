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
import org.openmhealth.shimmer.common.translator.DateTimeFormatTranslator;

import java.time.OffsetDateTime;


/**
 * @author Chris Schaefbauer
 */
public class DateTimeRequestEntityAssembler implements RequestEntityAssembler {


    private final Range<OffsetDateTime> timeRange;
    private DateTimeQuerySettings querySettings;


    public DateTimeRequestEntityAssembler(DateTimeQuerySettings querySettings, Range<OffsetDateTime> timeRange) {

        this.querySettings = querySettings;
        this.timeRange = timeRange;
    }

    @Override
    public RequestEntityBuilder assemble(RequestEntityBuilder builder, DataPointRequest request) {

        //EndpointSettings endpoint = request.getEndpointSettings();


        if (timeRange.hasLowerBound()) {

            if (!querySettings.getStartDateTimeParameter().isPresent()) {
                // Todo: throw configuration error
            }

            DateTimeRequestParameter startRequestParameter =
                    querySettings.getStartDateTimeParameter().get();

            DateTimeFormatTranslator formatTranslator = startRequestParameter.getDateTimeFormat();

            builder.addParameterWithValue(startRequestParameter, formatTranslator.translate(
                    timeRange.lowerEndpoint()));
        }

        if (querySettings.getEndDateTimeParameter().isPresent()) {

            // Todo: implement end effective time frame

        }


        return builder;
    }

}
