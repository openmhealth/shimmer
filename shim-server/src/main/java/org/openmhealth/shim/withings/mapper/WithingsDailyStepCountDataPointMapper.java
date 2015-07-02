package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.*;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsDailyStepCountDataPointMapper extends WithingsListDataPointMapper<StepCount> {


    @Override
    Optional<DataPoint<StepCount>> asDataPoint(JsonNode node) {
        long stepValue = asRequiredLong(node, "steps");
        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepValue);
        Optional<String> dateString = asOptionalString(node, "date");
        Optional<String> timeZoneFullName = asOptionalString(node, "timezone");

        if(dateString.isPresent()&&timeZoneFullName.isPresent()){
            LocalDateTime localDateTime = LocalDate.parse(dateString.get()).atStartOfDay();
            ZoneId zoneId = ZoneId.of(timeZoneFullName.get());
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
            ZoneOffset offset = zonedDateTime.getOffset();
            OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, offset);
            stepCountBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetDateTime,new DurationUnitValue(
                    DurationUnit.DAY,1)));
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if(userComment.isPresent()){
            stepCountBuilder.setUserNotes(userComment.get());
        }

        StepCount stepCount = stepCountBuilder.build();
        DataPoint<StepCount> stepCountDataPoint = newDataPoint(stepCount, RESOURCE_API_SOURCE_NAME, null, true);
        return Optional.of(stepCountDataPoint);
    }



    @Override
    String getListNodeName() {
        return "activities";
    }
}
