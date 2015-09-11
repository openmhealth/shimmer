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
import org.openmhealth.schema.domain.omh.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Fitbit Resource API activities/date responses to {@link PhysicalActivity} objects.
 *
 * @author Chris Schaefbauer
 */
public class FitbitPhysicalActivityDataPointMapper extends FitbitDataPointMapper<PhysicalActivity> {

    /**
     * Maps a JSON response node from the Fitbit API into a {@link PhysicalActivity} measure.
     *
     * @param node a JSON node for an individual object in the "activities" array retrieved from the activities/date/
     * Fitbit API endpoint
     * @return a {@link DataPoint} object containing a {@link PhysicalActivity} measure with the appropriate values from
     * the node parameter, wrapped as an {@link Optional}
     */
    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode node) {

        String activityName = asRequiredString(node, "name");
        PhysicalActivity.Builder activityBuilder = new PhysicalActivity.Builder(activityName);

        Boolean hasStartTime = asRequiredBoolean(node, "hasStartTime");

        //hasStartTime is true if the startTime value has been set, which is required of entries through the user GUI
        // and from sensed data,
        // however some of their data import workflows may set dummy values for these (00:00:00), in which case
        // hasStartTime is false and the time shouldn't be used
        if (hasStartTime) {

            Optional<LocalDateTime> localStartDateTime = asOptionalLocalDateTime(node, "startDate", "startTime");
            Optional<Long> duration = asOptionalLong(node, "duration");

            if (localStartDateTime.isPresent()) {

                OffsetDateTime offsetStartDateTime = combineDateTimeAndTimezone(localStartDateTime.get());
                if (duration.isPresent()) {
                    activityBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime,
                            new DurationUnitValue(DurationUnit.MILLISECOND, duration.get())));
                }
                else {
                    activityBuilder.setEffectiveTimeFrame(offsetStartDateTime);
                }

            }
        }
        else {

            Optional<LocalDate> localStartDate = asOptionalLocalDate(node, "startDate");

            if (localStartDate.isPresent()) {
                //In this case we have a date, but no time, so we set the startTime to beginning of day on the
                // startDate, add the offset, then set the duration as the entire day
                LocalDateTime localStartDateTime = localStartDate.get().atStartOfDay();
                OffsetDateTime offsetStartDateTime = combineDateTimeAndTimezone(localStartDateTime);
                activityBuilder.setEffectiveTimeFrame(TimeInterval
                        .ofStartDateTimeAndDuration(offsetStartDateTime, new DurationUnitValue(DurationUnit.DAY, 1)));
            }
        }

        Optional<Double> distance = asOptionalDouble(node, "distance");

        if (distance.isPresent()) {
            //by default fitbit returns metric unit values (https://wiki.fitbit.com/display/API/API+Unit+System), so
            // this assumes that the response is using the default for distance (KM)
            activityBuilder.setDistance(new LengthUnitValue(LengthUnit.KILOMETER, distance.get()));
        }

        PhysicalActivity measure = activityBuilder.build();
        Optional<Long> externalId = asOptionalLong(node, "logId");
        return Optional.of(newDataPoint(measure, externalId.orElse(null)));
    }

    /**
     * @return the name of the list node returned from the activities/date Fitbit endpoint
     */
    @Override
    protected String getListNodeName() {
        return "activities";
    }
}
