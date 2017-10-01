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
import org.openmhealth.schema.domain.omh.StepCount2;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.ZoneOffset.UTC;
import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLocalDate;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * A mapper from Misfit Resource API /activity/summary?detail=true responses to {@link StepCount2} objects.
 *
 * @author Emerson Farrugia
 * @author Eric Jain
 * @see <a href="https://build.misfit.com/docs/cloudapi/api_references#summary">API documentation</a>
 */
public class MisfitStepCountDataPointMapper extends MisfitDataPointMapper<StepCount2> {

    @Override
    protected String getListNodeName() {
        return "summary";
    }

    @Override
    public Optional<DataPoint<StepCount2>> asDataPoint(JsonNode summaryNode) {

        checkNotNull(summaryNode);

        Long stepCount = asRequiredLong(summaryNode, "steps");

        if (stepCount == 0) {
            return empty();
        }

        // this property isn't listed in the table, but does appear in the second Example section where detail is true
        LocalDate localDate = asRequiredLocalDate(summaryNode, "date");

        // FIXME fix the time zone offset once Misfit add it to the API
        OffsetDateTime startDateTime = localDate.atStartOfDay().atOffset(UTC);
        DurationUnitValue durationUnitValue = new DurationUnitValue(DAY, 1);

        TimeInterval effectiveTimeInterval = ofStartDateTimeAndDuration(startDateTime, durationUnitValue);

        StepCount2 measure = new StepCount2.Builder(stepCount, effectiveTimeInterval)
                .build();

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, null, null));
    }
}
