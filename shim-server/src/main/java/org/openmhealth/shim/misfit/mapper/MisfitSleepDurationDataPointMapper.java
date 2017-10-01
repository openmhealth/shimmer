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

package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration2;
import org.openmhealth.schema.domain.omh.SleepEpisode;

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
 * A mapper from Misfit Resource API /activity/sleeps responses to {@link SleepDuration2} objects. This mapper currently
 * creates a single data point per sleep node in the response, subtracting the duration of awake segments from the sleep
 * duration. It's also possible to create a single data point per sleep segment, which would help preserve the
 * granularity of the original data, but that may be better suited to {@link SleepEpisode} measures.
 *
 * @author Emerson Farrugia
 * @see <a href="https://build.misfit.com/docs/cloudapi/api_references#sleep">API documentation</a>
 */
public class MisfitSleepDurationDataPointMapper extends MisfitSleepMeasureDataPointMapper<SleepDuration2> {

    @Override
    protected String getListNodeName() {
        return "sleeps";
    }

    @Override
    public Optional<DataPoint<SleepDuration2>> asDataPoint(JsonNode sleepNode) {

        List<SleepSegment> sleepSegments = asSleepSegments(sleepNode);

        Optional<OffsetDateTime> effectiveStartDateTime = getSleepOnsetDateTime(sleepSegments);

        if (!effectiveStartDateTime.isPresent()) {
            return empty();
        }

        OffsetDateTime effectiveEndDateTime = getArisingDateTime(sleepSegments)
                .orElseThrow(IllegalStateException::new);

        long sleepDurationInSec = sleepSegments.stream()
                .filter((segment) -> segment.getType() != AWAKE)
                .mapToLong(SleepSegment::getDurationInSec)
                .sum();

        SleepDuration2 measure =
                new SleepDuration2.Builder(
                        new DurationUnitValue(SECOND, sleepDurationInSec),
                        ofStartDateTimeAndEndDateTime(effectiveStartDateTime.get(), effectiveEndDateTime)
                )
                        .build();

        String externalId = asOptionalString(sleepNode, "id").orElse(null);
        Boolean sensed = asOptionalBoolean(sleepNode, "autoDetected").orElse(null);

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId, sensed));
    }
}
