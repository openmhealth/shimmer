package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;

/**
 * @author Chris Schaefbauer
 */
public class FitbitSleepDurationDataPointMapper extends FitbitDataPointMapper<SleepDuration>{
    @Override
    protected Optional<DataPoint<SleepDuration>> asDataPoint(JsonNode node, int offsetInMilliseconds) {
        DurationUnitValue unitValue = new DurationUnitValue(DurationUnit.MINUTE,asRequiredDouble(node,"minutesAsleep"));
        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(unitValue);


        Optional<LocalDateTime> localStartTime = asOptionalLocalDateTime(node, "startTime");

        if(localStartTime.isPresent()){
            OffsetDateTime offsetStartDateTime = combineDateTimeAndTimezone(localStartTime.get(), offsetInMilliseconds);
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

    @Override
    protected String getListNodeName() {
        return "sleep";
    }
}
