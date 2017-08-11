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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.PercentUnit.PERCENT;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper that translates responses from the Fitbit Resource API <code>sleep/date</code> endpoint into {@link
 * SleepEpisode} data points.
 *
 * @author Emerson Farrugia
 * @see <a href="https://dev.fitbit.com/docs/sleep/#get-sleep-logs">API documentation</a>
 */
public class FitbitSleepEpisodeDataPointMapper extends FitbitSleepMeasureDataPointMapper<SleepEpisode> {

    private static final Logger logger = LoggerFactory.getLogger(FitbitSleepEpisodeDataPointMapper.class);


    @Override
    protected String getListNodeName() {
        return "sleep";
    }

    @Override
    protected Optional<DataPoint<SleepEpisode>> asDataPoint(JsonNode node) {

        Double totalSleepTimeInMinutes = asRequiredDouble(node, "minutesAsleep");

        if (totalSleepTimeInMinutes == 0) {
            return empty();
        }

        JsonNode sleepLevelsNode = asRequiredNode(node, "levels.data");

        Optional<OffsetDateTime> sleepOnsetDateTime = getSleepOnsetDateTime(sleepLevelsNode);

        if (!sleepOnsetDateTime.isPresent()) {
            logger.warn(
                    "The following Fitbit sleep log entry has positive 'minutesAsleep' but doesn't contain an asleep segment.\n{}",
                    node);
            return empty();
        }

        OffsetDateTime arisingDateTime = getArisingDateTime(sleepLevelsNode).orElseThrow(IllegalStateException::new);

        TimeInterval effectiveTimeInterval =
                TimeInterval.ofStartDateTimeAndEndDateTime(sleepOnsetDateTime.get(), arisingDateTime);

        SleepEpisode.Builder sleepEpisodeBuilder = new SleepEpisode.Builder(effectiveTimeInterval)
                .setTotalSleepTime(new DurationUnitValue(MINUTE, totalSleepTimeInMinutes));

        asOptionalDouble(node, "minutesToFallAsleep")
                .ifPresent((m) -> sleepEpisodeBuilder.setLatencyToSleepOnset(new DurationUnitValue(MINUTE, m)));

        asOptionalDouble(node, "minutesAfterWakeup")
                .ifPresent((m) -> sleepEpisodeBuilder.setLatencyToArising(new DurationUnitValue(MINUTE, m)));

        // not present on date range retrievals
        asOptionalBoolean(node, "isMainSleep").ifPresent(sleepEpisodeBuilder::setMainSleep);

        // not present on date range retrievals
        asOptionalDouble(node, "efficiency").ifPresent(
                (p) -> sleepEpisodeBuilder.setSleepMaintenanceEfficiencyPercentage(new TypedUnitValue<>(PERCENT, p)));

        SleepEpisode measure = sleepEpisodeBuilder.build();

        Optional<Long> externalId = asOptionalLong(node, "logId");

        return Optional.of(newDataPoint(measure, externalId.orElse(null)));
    }
}
