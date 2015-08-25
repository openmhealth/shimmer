package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DurationUnit;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/sleeps">API documentation</a>
 */
public class JawboneSleepDurationDataPointMapper extends JawboneDataPointMapper<SleepDuration> {

    @Override
    protected Optional<SleepDuration> getMeasure(JsonNode listEntryNode) {

        Long totalSleepSessionDuration = asRequiredLong(listEntryNode, "details.duration");
        Long totalTimeAwake = asRequiredLong(listEntryNode, "details.awake");

        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(
                new DurationUnitValue(DurationUnit.SECOND, totalSleepSessionDuration - totalTimeAwake));

        setEffectiveTimeFrame(sleepDurationBuilder, listEntryNode);

        SleepDuration sleepDuration = sleepDurationBuilder.build();
        asOptionalLong(listEntryNode, "details.awakenings")
                .ifPresent(wakeupCount -> sleepDuration.setAdditionalProperty("wakeup_count", wakeupCount));

        return Optional.of(sleepDuration);
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode) {

        Optional<Long> optionalLightSleep = asOptionalLong(listEntryNode, "details.light");
        Optional<Long> optionalAwakeTime = asOptionalLong(listEntryNode, "details.awake");
        if (optionalAwakeTime.isPresent() && optionalLightSleep.isPresent()) {
            if (optionalAwakeTime.get() > 0 || optionalLightSleep.get() > 0) {
                return true; // Jawbone documentation states that sleep details, specifically awake and light sleep
                // values, are only recorded when sleep has been sensed by a Jawbone wearable. If these values are
                // zero, however, this does not guarantee that the datapoint is user entered or unsensed.
            }
        }
        return false;
    }
}
