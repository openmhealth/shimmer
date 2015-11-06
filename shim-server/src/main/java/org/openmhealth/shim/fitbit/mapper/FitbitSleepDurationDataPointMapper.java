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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Fitbit Resource API <code>sleep/date</code> responses to {@link SleepDuration} objects.
 *
 * @author Chris Schaefbauer
 */
public class FitbitSleepDurationDataPointMapper extends FitbitDataPointMapper<SleepDuration> {

    @Override
    protected Optional<DataPoint<SleepDuration>> asDataPoint(JsonNode node) {

        DurationUnitValue unitValue = new DurationUnitValue(MINUTE, asRequiredDouble(node, "minutesAsleep"));
        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(unitValue);

        Optional<LocalDateTime> localStartTime = asOptionalLocalDateTime(node, "startTime");

        if (localStartTime.isPresent()) {

            OffsetDateTime offsetStartDateTime = combineDateTimeAndTimezone(localStartTime.get());
            Optional<Double> timeInBed = asOptionalDouble(node, "timeInBed");

            if (timeInBed.isPresent()) {
                sleepDurationBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime,
                        new DurationUnitValue(MINUTE, timeInBed.get())));
            }
            else {
                // in this case, there is no "time in bed" value, however we still have a start time, so we can set
                // the data point to a single date time point
                sleepDurationBuilder.setEffectiveTimeFrame(offsetStartDateTime);
            }
        }

        SleepDuration measure = sleepDurationBuilder.build();

        Optional<Long> externalId = asOptionalLong(node, "logId");

        return Optional.of(newDataPoint(measure, externalId.orElse(null)));
    }

    /**
     * @return the name of the list node returned from the sleep/date Fitbit endpoint
     */
    @Override
    protected String getListNodeName() {
        return "sleep";
    }
}
