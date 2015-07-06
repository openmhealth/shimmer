package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.*;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * A mapper from Withings Activity Measures endpoint responses (/measure?action=getactivity) to {@link StepCount}
 * objects
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_activity">Activity Measures API documentation</a>
 */
public class WithingsDailyStepCountDataPointMapper extends WithingsListDataPointMapper<StepCount> {

    /**
     * Maps an individual list node from the array in the Withings activity measure endpoint response into a {@link
     * StepCount} data point
     *
     * @param node activity node from the array "activites" contained in the "body" of the endpoint response
     * @return a {@link DataPoint} object containing a {@link StepCount} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    Optional<DataPoint<StepCount>> asDataPoint(JsonNode node) {
        long stepValue = asRequiredLong(node, "steps");
        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepValue);
        Optional<String> dateString = asOptionalString(node, "date");
        Optional<String> timeZoneFullName = asOptionalString(node, "timezone");

        if (dateString.isPresent() && timeZoneFullName.isPresent()) {
            LocalDateTime localDateTime = LocalDate.parse(dateString.get()).atStartOfDay();
            ZoneId zoneId = ZoneId.of(timeZoneFullName.get());
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
            ZoneOffset offset = zonedDateTime.getOffset();
            OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, offset);
            stepCountBuilder.setEffectiveTimeFrame(
                    TimeInterval.ofStartDateTimeAndDuration(offsetDateTime, new DurationUnitValue(
                            DurationUnit.DAY, 1)));
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            stepCountBuilder.setUserNotes(userComment.get());
        }

        StepCount stepCount = stepCountBuilder.build();
        DataPoint<StepCount> stepCountDataPoint = newDataPoint(stepCount, RESOURCE_API_SOURCE_NAME, null, true, null);
        return Optional.of(stepCountDataPoint);
    }

    /**
     * Returns the list name for splitting out individual activity measure items that can then be mapped.
     *
     * @return the name of the array containing the individual activity measure nodes
     */
    @Override
    String getListNodeName() {
        return "activities";
    }
}
