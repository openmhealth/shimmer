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

import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.DurationUnit.MILLISECOND;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.KILOMETER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper that translates responses from the Fitbit Resource API <code>activities/date</code> endpoint into {@link
 * PhysicalActivity} data points.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://dev.fitbit.com/docs/activity/#get-daily-activity-summary">API documentation</a>
 */
public class FitbitPhysicalActivityDataPointMapper extends FitbitDataPointMapper<PhysicalActivity> {

    @Override
    protected String getListNodeName() {
        return "activities";
    }

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode node) {

        String activityName = asRequiredString(node, "name");
        PhysicalActivity.Builder activityBuilder = new PhysicalActivity.Builder(activityName);

        Boolean hasStartTime = asRequiredBoolean(node, "hasStartTime");

        /*
         * hasStartTime is true if the startTime value has been set, which is required of entries through the user
         * GUI and from sensed data, however some of their data import workflows may set dummy values for these
         * (00:00:00), in which case hasStartTime is false and the time shouldn't be used
         */
        if (hasStartTime) {

            Optional<LocalDateTime> localStartDateTime = asOptionalLocalDateTime(node, "startDate", "startTime");
            Optional<Long> duration = asOptionalLong(node, "duration");

            if (localStartDateTime.isPresent()) {

                OffsetDateTime offsetStartDateTime = asOffsetDateTimeWithFakeUtcTimeZone(localStartDateTime.get());
                if (duration.isPresent()) {
                    activityBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime,
                            new DurationUnitValue(MILLISECOND, duration.get())));
                }
                else {
                    activityBuilder.setEffectiveTimeFrame(offsetStartDateTime);
                }
            }
        }
        else {
            Optional<LocalDate> localStartDate = asOptionalLocalDate(node, "startDate");

            if (localStartDate.isPresent()) {
                // in this case we have a date, but no time, so we set the startTime to beginning of day on the
                // startDate, add the offset, then set the duration as the entire day
                LocalDateTime localStartDateTime = localStartDate.get().atStartOfDay();
                OffsetDateTime offsetStartDateTime = asOffsetDateTimeWithFakeUtcTimeZone(localStartDateTime);
                activityBuilder.setEffectiveTimeFrame(TimeInterval
                        .ofStartDateTimeAndDuration(offsetStartDateTime, new DurationUnitValue(DAY, 1)));
            }
        }

        Optional<Double> distance = asOptionalDouble(node, "distance");

        if (distance.isPresent()) {
            // by default fitbit returns metric unit values (https://wiki.fitbit.com/display/API/API+Unit+System), so
            // this assumes that the response is using the default for distance (KM)
            activityBuilder.setDistance(new LengthUnitValue(KILOMETER, distance.get()));
        }

        asOptionalDouble(node, "calories")
                .ifPresent(calories -> activityBuilder.setCaloriesBurned(new KcalUnitValue(KILOCALORIE, calories)));

        PhysicalActivity measure = activityBuilder.build();
        Optional<Long> externalId = asOptionalLong(node, "logId");

        return Optional.of(newDataPoint(measure, externalId.orElse(null)));
    }
}
