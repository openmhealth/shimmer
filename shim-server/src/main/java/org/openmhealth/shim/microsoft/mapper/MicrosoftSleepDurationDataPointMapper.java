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

package org.openmhealth.shim.microsoft.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration;
import org.openmhealth.shim.common.mapper.JsonNodeMappingException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Microsoft Resource API /activity/sleeps responses to {@link SleepDuration} objects. This mapper
 * currently creates a single data point per sleep node in the response, subtracting the duration of awake segments
 * from the sleep duration. It's also possible to create a single data point per sleep segment, which would help
 * preserve the granularity of the original data. This mapper may be updated to return a data point per segment in the
 * future.
 *
 */
public class MicrosoftSleepDurationDataPointMapper extends MicrosoftDataPointMapper<SleepDuration> {

    public static final int AWAKE_SEGMENT_TYPE = 1;

    @Override
    protected String getListNodeName() {
        return "sleepActivities";
    }

    @Override
    public Optional<DataPoint<SleepDuration>> asDataPoint(JsonNode sleepNode) {

        // to calculate the duration of last segment, first determine the overall end time
        OffsetDateTime startDateTime = asRequiredOffsetDateTime(sleepNode, "startTime");
        OffsetDateTime endDateTime = asRequiredOffsetDateTime(sleepNode, "endTime");

        if (startDateTime == null) {
            throw new JsonNodeMappingException(format("The Microsoft sleep node '%s' has no sleep details.", sleepNode));
        }

        String sleepDurationString = asRequiredString(sleepNode, "sleepDuration");
        Duration duration = java.time.Duration.parse(sleepDurationString);


        SleepDuration measure = new SleepDuration.Builder(new DurationUnitValue(SECOND, duration.get(SECONDS)))
                .setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(startDateTime, endDateTime))
                .build();

        String externalId = asOptionalString(sleepNode, "id").orElse(null);

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId, null));
    }
}
