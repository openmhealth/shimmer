/*
 * Copyright 2017 Open mHealth
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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount2;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Fitbit Resource API <code>activities/date</code> responses to {@link StepCount2} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class FitbitStepCountDataPointMapper extends FitbitDataPointMapper<StepCount2> {

    /**
     * @return the name of the list node returned from the activities/steps Fitbit endpoint
     */
    @Override
    protected String getListNodeName() {
        return "activities-steps";
    }

    @Override
    protected Optional<DataPoint<StepCount2>> asDataPoint(JsonNode node) {

        // TODO consider adding type casting support to mapper tools
        // Integer stepCountValue = asRequiredInteger(node, "value");
        int stepCountValue = Integer.parseInt(asRequiredString(node, "value"));

        if (stepCountValue == 0) {
            return Optional.empty();
        }

        LocalDate effectiveLocalDate = asRequiredLocalDate(node, "dateTime");

        OffsetDateTime effectiveStartDateTime = asOffsetDateTimeWithFakeUtcTimeZone(effectiveLocalDate.atStartOfDay());

        TimeInterval effectiveTimeInterval =
                TimeInterval.ofStartDateTimeAndDuration(effectiveStartDateTime, new DurationUnitValue(DAY, 1));

        StepCount2 measure = new StepCount2.Builder(stepCountValue, effectiveTimeInterval).build();

        Optional<Long> externalId = asOptionalLong(node, "logId");

        return Optional.of(newDataPoint(measure, externalId.orElse(null)));
    }
}
