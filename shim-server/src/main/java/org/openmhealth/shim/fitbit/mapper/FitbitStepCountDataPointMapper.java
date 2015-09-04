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
 * A mapper from Fitbit Resource API activities/date responses to {@link StepCount} objects
 * @author Chris Schaefbauer
 */
public class FitbitStepCountDataPointMapper extends FitbitDataPointMapper<StepCount> {

    /**
     * Maps a JSON response node from the Fitbit API into a {@link StepCount} measure
     * @param node a JSON node for an individual object in the "activities-steps" array retrieved from the activities/steps
     * Fitbit API endpoint
     * @return a {@link DataPoint} object containing a {@link StepCount} measure with the appropriate values from
     * the node parameter, wrapped as an {@link Optional}
     */
    @Override
    protected Optional<DataPoint<StepCount>> asDataPoint(JsonNode node) {

        StepCount.Builder builder = new StepCount.Builder(Integer.parseInt(asRequiredString(node,"value")));

        Optional<LocalDate> stepDate = asOptionalLocalDate(node, "dateTime");
        if(stepDate.isPresent()){
            LocalDateTime startDateTime = stepDate.get().atTime(0,0,0,0);
            LocalDateTime endDateTime = stepDate.get().atTime(23,59,59,999999999);

            builder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(combineDateTimeAndTimezone(startDateTime),
                    combineDateTimeAndTimezone(endDateTime)));

        }

        StepCount measure=builder.build();
        Optional<Long> externalId = asOptionalLong(node,"logId");
        return Optional.of(newDataPoint(measure, externalId.orElse(null)));
    }

    /**
     * @return the name of the list node returned from the activities/steps Fitbit endpoint
     */
    @Override
    protected String getListNodeName() {
        return "activities-steps";
    }
}
