package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;

/**
 * A mapper from Fitbit Resource API sleep/date responses to {@link SleepDuration} objects
 * @author Chris Schaefbauer
 */
public class FitbitSleepDurationDataPointMapper extends FitbitDataPointMapper<SleepDuration>{

    /**
     * Maps a JSON response node from the Fitbit API into a {@link SleepDuration} measure
     * @param node a JSON node for an individual object in the "sleep" array retrieved from the sleep/date/
     * Fitbit API endpoint
     * @param offsetFromUTCInMilliseconds the "offsetFromUTCMillis" property from a JSON response from the
     * user/<user-id>/profile Fitbit API endpoint, may be incorrect if the user has changed time zone since the data point
     * was created
     * @return a {@link DataPoint} object containing a {@link SleepDuration} measure with the appropriate values from
     * the node parameter, wrapped as an {@link Optional}
     */
    @Override
    protected Optional<DataPoint<SleepDuration>> asDataPoint(JsonNode node, int offsetFromUTCInMilliseconds) {
        DurationUnitValue unitValue = new DurationUnitValue(DurationUnit.MINUTE,asRequiredDouble(node,"minutesAsleep"));
        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(unitValue);


        Optional<LocalDateTime> localStartTime = asOptionalLocalDateTime(node, "startTime");

        if(localStartTime.isPresent()){
            OffsetDateTime offsetStartDateTime = combineDateTimeAndTimezone(localStartTime.get(),
                    offsetFromUTCInMilliseconds);
            Optional<Double> timeInBed = asOptionalDouble(node, "timeInBed");
            if(timeInBed.isPresent()){
                sleepDurationBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime,new DurationUnitValue(DurationUnit.MINUTE,timeInBed.get())));
            }
            else{
                //in this case, there is no "time in bed" value, however we still have a start time, so we can set the datapoint to a single datetime point
                sleepDurationBuilder.setEffectiveTimeFrame(offsetStartDateTime);
            }
        }

        SleepDuration measure = sleepDurationBuilder.build();

        Optional<Long> externalId = asOptionalLong(node, "logId");
        return Optional.of(newDataPoint(measure,externalId.orElse(null)));
    }

    /**
     * @return the name of the list node returned from the sleep/date Fitbit endpoint
     */
    @Override
    protected String getListNodeName() {
        return "sleep";
    }
}
