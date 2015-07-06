package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.ZoneId.of;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Created by Chris Schaefbauer on 7/6/15.
 */
public class WithingsSleepDurationDataPointMapper extends WithingsListDataPointMapper<SleepDuration>{

    @Override
    Optional<DataPoint<SleepDuration>> asDataPoint(JsonNode node) {

        Long lightSleepInSeconds = asRequiredLong(node,"data.lightsleepduration");
        Long deepSleepInSeconds = asRequiredLong(node, "data.deepsleepduration");
        Long remSleepInSeconds = asRequiredLong(node, "data.remsleepduration");

        Long totalSleepInSeconds = lightSleepInSeconds+deepSleepInSeconds+remSleepInSeconds;

        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(new DurationUnitValue(DurationUnit.SECOND,totalSleepInSeconds));


        Optional<Long> startDateInEpochSeconds = asOptionalLong(node, "startdate");
        Optional<Long> endDateInEpochSeconds = asOptionalLong(node, "enddate");
        Optional<String> timezone = asOptionalString(node, "timezone");
        if(startDateInEpochSeconds.isPresent()&&endDateInEpochSeconds.isPresent()&&timezone.isPresent()){
            OffsetDateTime offsetStartDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(startDateInEpochSeconds.get()),of(
                    timezone.get()));
            OffsetDateTime offsetEndDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(endDateInEpochSeconds.get()),of(timezone.get()));
            sleepDurationBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(offsetStartDateTime,offsetEndDateTime));
        }

        Optional<Long> externalId = asOptionalLong(node, "id");
        Optional<Long> modelId = asOptionalLong(node, "model");
        String modelName = null;

        if(modelId.isPresent()){
            modelName = SleepDeviceTypes.valueOf(modelId.get());
        }

        SleepDuration sleepDuration = sleepDurationBuilder.build();
        Optional<Long> wakeupCount = asOptionalLong(node, "data.wakeupcount");
        if(wakeupCount.isPresent()){
            sleepDuration.setAdditionalProperty("wakeup_count",new Integer(wakeupCount.get().intValue()));
        }

        return Optional.of(newDataPoint(sleepDuration,RESOURCE_API_SOURCE_NAME,externalId.orElse(null),true, modelName));
    }

    @Override
    String getListNodeName() {
        return "series";
    }

    public enum SleepDeviceTypes{
        Pulse(16), Aura(32);

        private long deviceId;

        private static Map<Long, String> map = new HashMap<Long, String>();

        static {
            for (SleepDeviceTypes sleepDeviceTypeName : SleepDeviceTypes.values()) {
                map.put(sleepDeviceTypeName.deviceId, sleepDeviceTypeName.name());
            }
        }

        private SleepDeviceTypes(final long deviceId) { this.deviceId = deviceId; }

        public static String valueOf(long deviceId) {
            return map.get(deviceId);
        }
    }
}
