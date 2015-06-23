package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;

/**
 * @author Chris Schaefbauer
 */
public class FitbitStepCountDataPointMapper extends FitbitDataPointMapper {
    @Override
    protected Optional<DataPoint> asDataPoint(JsonNode node, int offsetInMilliseconds) {

        StepCount.Builder builder = new StepCount.Builder(Integer.parseInt(asRequiredString(node,"value")));

        //LocalDate stepDate = LocalDate.parse(node.get("dateTime").asText(),DateTimeFormatter.ISO_LOCAL_DATE);
        Optional<LocalDate> stepDate = asOptionalLocalDate(node, "dateTime");

        if(stepDate.isPresent()){
            LocalDateTime startDateTime = stepDate.get().atTime(0,0,0,0);
            LocalDateTime endDateTime = stepDate.get().atTime(23,59,59,999999999);

//            builder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(combineDateTimeAndTimezone(startTime.get().withHour(0).withMinute(0).withSecond(0).withNano(0), offsetInMilliseconds),
//                    combineDateTimeAndTimezone(endTime.get().withHour(23).withMinute(59).withSecond(59).withNano(999999999), offsetInMilliseconds)));
            builder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(combineDateTimeAndTimezone(startDateTime, offsetInMilliseconds),
                    combineDateTimeAndTimezone(endDateTime, offsetInMilliseconds)));

        }

        StepCount measure=builder.build();
        Optional<Long> externalId = asOptionalLong(node,"logId");
        return Optional.of(newDataPoint(measure, externalId.orElse(null)));
    }

    @Override
    protected String getListNodeName() {
        return "activities-steps";
    }
}
