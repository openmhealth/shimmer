/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.ZoneId.of;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * A mapper from Withings Sleep Summary endpoint responses (/sleep?action=getsummary) to {@link SleepDuration1}
 * objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_sleep_summary">Sleep Summary API documentation</a>
 */
public class WithingsSleepDurationDataPointMapper extends WithingsListDataPointMapper<SleepDuration1> {

    /**
     * Maps an individual list node from the array in the Withings sleep summary endpoint response into a {@link
     * SleepDuration1} data point.
     *
     * @param node activity node from the array "series" contained in the "body" of the endpoint response
     * @return a {@link DataPoint} object containing a {@link SleepDuration1} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    Optional<DataPoint<SleepDuration1>> asDataPoint(JsonNode node) {

        Long lightSleepInSeconds = asRequiredLong(node, "data.lightsleepduration");
        Long deepSleepInSeconds = asRequiredLong(node, "data.deepsleepduration");
        Long remSleepInSeconds = asRequiredLong(node, "data.remsleepduration");

        Long totalSleepInSeconds = lightSleepInSeconds + deepSleepInSeconds + remSleepInSeconds;

        SleepDuration1.Builder sleepDurationBuilder =
                new SleepDuration1.Builder(new DurationUnitValue(DurationUnit.SECOND, totalSleepInSeconds));


        Optional<Long> startDateInEpochSeconds = asOptionalLong(node, "startdate");
        Optional<Long> endDateInEpochSeconds = asOptionalLong(node, "enddate");

        if (startDateInEpochSeconds.isPresent() && endDateInEpochSeconds.isPresent()) {
            OffsetDateTime offsetStartDateTime =
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(startDateInEpochSeconds.get()), of("Z"));
            OffsetDateTime offsetEndDateTime =
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(endDateInEpochSeconds.get()), of("Z"));
            sleepDurationBuilder.setEffectiveTimeFrame(
                    TimeInterval.ofStartDateTimeAndEndDateTime(offsetStartDateTime, offsetEndDateTime));
        }

        Optional<Long> externalId = asOptionalLong(node, "id");
        Optional<Long> modelId = asOptionalLong(node, "model");
        String modelName = null;

        if (modelId.isPresent()) {
            modelName = SleepDeviceTypes.valueOf(modelId.get());
        }

        SleepDuration1 sleepDuration = sleepDurationBuilder.build();
        Optional<Long> wakeupCount = asOptionalLong(node, "data.wakeupcount");
        if (wakeupCount.isPresent()) {
            sleepDuration.setAdditionalProperty("wakeup_count", new Integer(wakeupCount.get().intValue()));
        }

        // These sleep phase values are Withings platform-specific, so we pass them through as additionalProperties to
        // ensure we keep relevant platform specific values. Should be interpreted according to Withings API spec
        sleepDuration.setAdditionalProperty("light_sleep_duration",
                new DurationUnitValue(DurationUnit.SECOND, lightSleepInSeconds));
        sleepDuration.setAdditionalProperty("deep_sleep_duration",
                new DurationUnitValue(DurationUnit.SECOND, deepSleepInSeconds));
        sleepDuration.setAdditionalProperty("rem_sleep_duration",
                new DurationUnitValue(DurationUnit.SECOND, remSleepInSeconds));

        // This is an additional piece of information captured by Withings devices around sleep and should be
        // interpreted according to the Withings API specification. We do not capture durationtowakeup or
        // wakeupduration properties from the Withings API because it is unclear the distinction between them and we
        // aim to avoid creating more ambiguity through passing through these properties
        Optional<Long> timeToSleepValue = asOptionalLong(node, "data.durationtosleep");
        if (timeToSleepValue.isPresent()) {
            sleepDuration.setAdditionalProperty("duration_to_sleep",
                    new DurationUnitValue(DurationUnit.SECOND, timeToSleepValue.get()));
        }

        return Optional.of(newDataPoint(sleepDuration, externalId.orElse(null), true, modelName));
    }

    @Override
    String getListNodeName() {
        return "series";
    }

    // TODO clean this up
    public enum SleepDeviceTypes {
        Pulse(16), Aura(32);

        private long deviceId;

        private static Map<Long, String> map = new HashMap<Long, String>();

        static {
            for (SleepDeviceTypes sleepDeviceTypeName : SleepDeviceTypes.values()) {
                map.put(sleepDeviceTypeName.deviceId, sleepDeviceTypeName.name());
            }
        }

        SleepDeviceTypes(final long deviceId) {
            this.deviceId = deviceId;
        }

        /**
         * Returns the string device name for a device ID
         *
         * @param deviceId the id number for the device contained within the Withings API response datapoint
         * @return common name of the device (e.g., Pulse, Aura)
         */
        public static String valueOf(long deviceId) {
            return map.get(deviceId);
        }
    }
}
