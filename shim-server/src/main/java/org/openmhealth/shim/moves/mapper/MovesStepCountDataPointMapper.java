package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLocalDate;

/**
 * A mapper from Moves Resource API /user/summary/daily responses to {@link StepCount} objects.
 *
 * @author Jared Sieling.
 */
public class MovesStepCountDataPointMapper extends MovesDataPointMapper<StepCount>{

    @Override
    protected Optional<DataPoint<StepCount>> asDataPoint(JsonNode node) {

        // Sum steps from all individual activities
        int stepCountValue = 0;
        for(JsonNode activity : node.get("summary")){
            if(activity.has("steps")){
                stepCountValue = stepCountValue + activity.get("steps").asInt();
            }
        }

        if (stepCountValue == 0) {
            return Optional.empty();
        }

        StepCount.Builder builder = new StepCount.Builder(stepCountValue);

        Optional<LocalDate> stepDate = asOptionalLocalDate(node, "date", DateTimeFormatter.BASIC_ISO_DATE);

        if (stepDate.isPresent()) {
            LocalDateTime startDateTime = stepDate.get().atTime(0, 0, 0, 0);

            builder.setEffectiveTimeFrame(
                    TimeInterval.ofStartDateTimeAndDuration(combineDateTimeAndTimezone(startDateTime),
                            new DurationUnitValue(DAY, 1)));
        }

        StepCount measure = builder.build();

        return Optional.of(newDataPoint(measure, null));
    }
}
