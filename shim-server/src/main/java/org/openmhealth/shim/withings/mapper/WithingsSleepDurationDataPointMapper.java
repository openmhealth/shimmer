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
 * A mapper from Withings Sleep Summary endpoint responses (/sleep?action=getsummary) to {@link SleepDuration}
 * objects
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_sleep_summary">Sleep Summary API documentation</a>
 */
public class WithingsSleepDurationDataPointMapper extends WithingsListDataPointMapper<SleepDuration>{

    /**
     * Maps an individual list node from the array in the Withings sleep summary endpoint response into a {@link
     * SleepDuration} data point
     *
     * @param node activity node from the array "series" contained in the "body" of the endpoint response
     * @return a {@link DataPoint} object containing a {@link SleepDuration} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
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

    /**
     * The list name for splitting out individual sleep summary items that can then be mapped.
     *
     * @return the name of the array containing the individual sleep summary nodes
     */
    @Override
    String getListNodeName() {
        return "series";
    }

    /**
     * Enum mapping the different sleep tracking device names to the integer value used by Withings to identify them in datapoints
     */
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

        /**
         * Returns the string device name for a device ID
         * @param deviceId the id number for the device contained within the Withings API response datapoint
         * @return common name of the device (e.g., Pulse, Aura)
         */
        public static String valueOf(long deviceId) {
            return map.get(deviceId);
        }
    }
}
