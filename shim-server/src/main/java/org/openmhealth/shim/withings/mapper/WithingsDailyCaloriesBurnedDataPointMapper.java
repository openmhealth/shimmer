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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.*;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * A mapper from Withings Activity Measures endpoint responses (/measure?action=getactivity) to {@link CaloriesBurned}
 * objects
 * <p>
 * <p>NOTE: This only captures calories that are burned from activity that is captured by a Withings device or
 * application, and
 * may not be an accurate representation of all the calories burned from metabolic resting or activities not
 * captured.</p>
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_activity">Activity Measures API documentation</a>
 */
public class WithingsDailyCaloriesBurnedDataPointMapper extends WithingsListDataPointMapper<CaloriesBurned> {

    /**
     * Maps an individual list node from the array in the Withings activity measure endpoint response into a {@link
     * CaloriesBurned} data point.
     * <p>
     * <p>Note: the start datetime and end datetime values for the mapped {@link CaloriesBurned} {@link DataPoint}
     * assume that
     * the start timezone and end time zone are the same, both equal to the "timezone" property in the Withings
     * response
     * datapoints. However, according to Withings, the property value they provide is specifically the end datetime
     * timezone.</p>
     *
     * @param node activity node from the array "activites" contained in the "body" of the endpoint response
     * @return a {@link DataPoint} object containing a {@link CaloriesBurned} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    Optional<DataPoint<CaloriesBurned>> asDataPoint(JsonNode node) {

        long caloriesBurnedValue = asRequiredLong(node, "calories");
        CaloriesBurned.Builder caloriesBurnedBuilder =
                new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE, caloriesBurnedValue));

        Optional<String> dateString = asOptionalString(node, "date");
        Optional<String> timeZoneFullName = asOptionalString(node, "timezone");
        // We assume that timezone is the same for both the startdate and enddate timestamps, even though Withings only
        // provides the enddate timezone as the "timezone" property.
        // TODO: Revisit once Withings can provide start_timezone and end_timezone
        if (dateString.isPresent() && timeZoneFullName.isPresent()) {
            LocalDateTime localStartDateTime = LocalDate.parse(dateString.get()).atStartOfDay();
            ZoneId zoneId = ZoneId.of(timeZoneFullName.get());
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localStartDateTime, zoneId);
            ZoneOffset offset = zonedDateTime.getOffset();
            OffsetDateTime offsetStartDateTime = OffsetDateTime.of(localStartDateTime, offset);
            LocalDateTime localEndDateTime = LocalDate.parse(dateString.get()).atStartOfDay().plusDays(1);

            OffsetDateTime offsetEndDateTime = OffsetDateTime.of(localEndDateTime, offset);
            caloriesBurnedBuilder.setEffectiveTimeFrame(
                    TimeInterval.ofStartDateTimeAndEndDateTime(offsetStartDateTime,
                            offsetEndDateTime));
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            caloriesBurnedBuilder.setUserNotes(userComment.get());
        }

        CaloriesBurned caloriesBurned = caloriesBurnedBuilder.build();
        DataPoint<CaloriesBurned> caloriesBurnedDataPoint =
                newDataPoint(caloriesBurned, null, true, null);

        return Optional.of(caloriesBurnedDataPoint);

    }

    @Override
    String getListNodeName() {
        return "activities";
    }
}
