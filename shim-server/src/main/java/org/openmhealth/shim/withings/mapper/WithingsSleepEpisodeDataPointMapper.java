/*
 * Copyright 2017 Open mHealth
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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepEpisode;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Withings Sleep Summary endpoint responses (/sleep?action=getsummary) to {@link SleepEpisode} objects.
 *
 * @author Emerson Farrugia
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_sleep_summary">Sleep Summary API documentation</a>
 */
public class WithingsSleepEpisodeDataPointMapper extends WithingsListDataPointMapper<SleepEpisode> {

    @Override
    String getListNodeName() {
        return "series";
    }

    /**
     * Maps an individual list node from the array in the Withings sleep summary endpoint response into a {@link
     * SleepEpisode} data point.
     *
     * @param node activity node from the array "series" contained in the "body" of the endpoint response
     * @return a {@link SleepEpisode} data point
     */
    @Override
    Optional<DataPoint<SleepEpisode>> asDataPoint(JsonNode node) {

        Long latencyToSleepOnsetInSeconds = asOptionalLong(node, "data.durationtosleep").orElse(0L);
        Long latencyToArisingInSeconds = asOptionalLong(node, "data.durationtowakeup").orElse(0L);
        String timeZoneId = asRequiredString(node, "timezone");

        OffsetDateTime effectiveStartDateTime = asOffsetDateTime(asRequiredLong(node, "startdate"), timeZoneId)
                .plusSeconds(latencyToSleepOnsetInSeconds);

        OffsetDateTime effectiveEndDateTime = asOffsetDateTime(asRequiredLong(node, "enddate"), timeZoneId)
                .minusSeconds(latencyToArisingInSeconds);

        SleepEpisode.Builder sleepEpisodeBuilder =
                new SleepEpisode.Builder(ofStartDateTimeAndEndDateTime(effectiveStartDateTime, effectiveEndDateTime))
                        .setLatencyToSleepOnset(new DurationUnitValue(SECOND, latencyToSleepOnsetInSeconds))
                        .setLatencyToArising(new DurationUnitValue(SECOND, latencyToArisingInSeconds));

        Long lightSleepDurationInSeconds = asOptionalLong(node, "data.lightsleepduration").orElse(0L);
        Long deepSleepDurationInSeconds = asOptionalLong(node, "data.deepsleepduration").orElse(0L);
        Long remSleepDurationInSeconds = asOptionalLong(node, "data.remsleepduration").orElse(0L);

        Long totalSleepDurationInSeconds =
                lightSleepDurationInSeconds + deepSleepDurationInSeconds + remSleepDurationInSeconds;

        sleepEpisodeBuilder.setTotalSleepTime(new DurationUnitValue(SECOND, totalSleepDurationInSeconds));

        asOptionalInteger(node, "data.wakeupcount").ifPresent(sleepEpisodeBuilder::setNumberOfAwakenings);

        SleepEpisode sleepEpisode = sleepEpisodeBuilder.build();

        // These sleep phase values are Withings platform-specific, so we pass them through as additionalProperties to
        // ensure we keep relevant platform specific values.
        sleepEpisode.setAdditionalProperty("light_sleep_duration",
                new DurationUnitValue(SECOND, lightSleepDurationInSeconds));
        sleepEpisode.setAdditionalProperty("deep_sleep_duration",
                new DurationUnitValue(SECOND, deepSleepDurationInSeconds));
        sleepEpisode.setAdditionalProperty("rem_sleep_duration",
                new DurationUnitValue(SECOND, remSleepDurationInSeconds));

        Optional<String> externalId = asOptionalLong(node, "id").map(Object::toString);

        WithingsDevice device = asOptionalInteger(node, "model")
                .flatMap(WithingsDevice::findByMagicNumber)
                .orElse(null);

        return Optional.of(newDataPoint(sleepEpisode, externalId.orElse(null), true, device));
    }
}
