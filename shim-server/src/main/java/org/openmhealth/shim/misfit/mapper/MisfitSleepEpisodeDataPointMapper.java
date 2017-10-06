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

package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepEpisode;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalBoolean;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.misfit.mapper.MisfitSleepMeasureDataPointMapper.SleepSegmentType.AWAKE;


/**
 * A mapper from Misfit Resource API /activity/sleeps responses to {@link SleepEpisode} objects.
 *
 * @author Emerson Farrugia
 * @see <a href="https://build.misfit.com/docs/cloudapi/api_references#sleep">API documentation</a>
 */
public class MisfitSleepEpisodeDataPointMapper extends MisfitSleepMeasureDataPointMapper<SleepEpisode> {

    @Override
    protected String getListNodeName() {
        return "sleeps";
    }

    @Override
    public Optional<DataPoint<SleepEpisode>> asDataPoint(JsonNode sleepNode) {

        List<SleepSegment> sleepSegments = asSleepSegments(sleepNode);

        Optional<TimeInterval> effectiveTimeInterval = getEffectiveTimeInterval(sleepSegments);

        if (!effectiveTimeInterval.isPresent()) {
            return empty();
        }

        SleepEpisode.Builder measureBuilder = new SleepEpisode.Builder(effectiveTimeInterval.get());

        long sleepDurationInSec = sleepSegments.stream()
                .filter((segment) -> segment.getType() != AWAKE)
                .mapToLong(SleepSegment::getDurationInSec)
                .sum();

        measureBuilder.setTotalSleepTime(new DurationUnitValue(SECOND, sleepDurationInSec));

        SleepEpisode measure = measureBuilder.build();

        String externalId = asOptionalString(sleepNode, "id").orElse(null);
        Boolean sensed = asOptionalBoolean(sleepNode, "autoDetected").orElse(null);

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId, sensed));
    }

    private Optional<TimeInterval> getEffectiveTimeInterval(List<SleepSegment> sleepSegments) {

        Optional<OffsetDateTime> effectiveStartDateTime = getSleepOnsetDateTime(sleepSegments);

        if (!effectiveStartDateTime.isPresent()) {
            return empty();
        }

        OffsetDateTime effectiveEndDateTime = getArisingDateTime(sleepSegments)
                .orElseThrow(IllegalStateException::new);

        return Optional.of(ofStartDateTimeAndEndDateTime(effectiveStartDateTime.get(), effectiveEndDateTime));
    }
}
