package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DurationUnit;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 */
public class JawboneSleepDurationDataPointMapper extends JawboneDataPointMapper<SleepDuration> {

    @Override
    protected Optional<SleepDuration> getMeasure(JsonNode listEntryNode) {
        Long totalSleepSessionDuration = asRequiredLong(listEntryNode, "details.duration");
        Long totalTimeAwake = asRequiredLong(listEntryNode, "details.awake");

        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(
                new DurationUnitValue(DurationUnit.SECOND, totalSleepSessionDuration - totalTimeAwake));
        Optional<Long> startDateTime = asOptionalLong(listEntryNode, "time_created");
        Optional<Long> endDateTime = asOptionalLong(listEntryNode, "time_completed");
        if (startDateTime.isPresent() && endDateTime.isPresent()) {

            ZoneId timeZoneId = ZoneId.of("Z");

            Optional<String> timeZoneString = asOptionalString(listEntryNode, "details.tz");
            if (timeZoneString.isPresent()) {
                timeZoneId = ZoneId.of(timeZoneString.get());
            }

            sleepDurationBuilder
                    .setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(startDateTime.get()), timeZoneId), OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(endDateTime.get()), timeZoneId)));

        }

        SleepDuration sleepDuration = sleepDurationBuilder.build();
        asOptionalLong(listEntryNode, "details.awakenings")
                .ifPresent(wakeupCount -> sleepDuration.setAdditionalProperty("wakeup_count", wakeupCount));

        return Optional.of(sleepDuration);
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode){
        Optional<Long> optionalLightSleep = asOptionalLong(listEntryNode, "details.light");
        Optional<Long> optionalAwakeTime = asOptionalLong(listEntryNode, "details.awake");
        if(optionalAwakeTime.isPresent()&&optionalLightSleep.isPresent()){
            if(optionalAwakeTime.get()>0 || optionalLightSleep.get()>0){
                return true;
            }
        }
        return false;
    }
}
