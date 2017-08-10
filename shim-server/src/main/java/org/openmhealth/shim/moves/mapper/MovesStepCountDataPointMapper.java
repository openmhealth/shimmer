package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount1;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLocalDate;

/**
 * A mapper from Moves Resource API /user/summary/daily responses to {@link StepCount1} objects.
 *
 * @author Jared Sieling
 * @see <a href="https://dev.moves-app.com/docs/api_summaries">API documentation</a>
 */
public class MovesStepCountDataPointMapper extends MovesDataPointMapper<StepCount1>{

    @Override
    protected Optional<DataPoint<StepCount1>> asDataPoint(JsonNode node) {

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

        StepCount1.Builder builder = new StepCount1.Builder(stepCountValue);

        Optional<LocalDate> stepDate = asOptionalLocalDate(node, "date", DateTimeFormatter.BASIC_ISO_DATE);

        if (stepDate.isPresent()) {
            LocalDateTime startDateTime = stepDate.get().atTime(0, 0, 0, 0);

            // FIXME the time zone handling here is suspect; if the code is going to assume UTC, the shim should be
            // asking for UTC in the initial request
            builder.setEffectiveTimeFrame(
                    TimeInterval.ofStartDateTimeAndDuration(combineDateTimeAndTimezone(startDateTime),
                            new DurationUnitValue(DAY, 1)));
        }

        StepCount1 measure = builder.build();

        return Optional.of(newDataPoint(measure, null));
    }
}
