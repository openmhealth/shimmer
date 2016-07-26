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
import org.openmhealth.schema.domain.omh.StepCount;
import org.openmhealth.shim.common.mapper.IncompatibleJsonNodeMappingException;
import org.openmhealth.shim.common.mapper.MissingJsonNodeMappingException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.ZoneOffset.UTC;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLocalDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * A mapper from Microsoft Resource API /activity/summary?detail=true responses to {@link StepCount} objects.
 *
 * @author Emerson Farrugia
 * @author Eric Jain
 * @see <a href="https://build.misfit.com/docs/references#APIReferences-Summary">API documentation</a>
 */
public class MicrosoftStepCountDataPointMapper extends MicrosoftDataPointMapper<StepCount> {

    @Override
    protected String getListNodeName() {
        return "summaries";
    }

    @Override
    public Optional<DataPoint<StepCount>> asDataPoint(JsonNode summaryNode) {

        checkNotNull(summaryNode);

        Long stepCount = asRequiredLong(summaryNode, "stepsTaken");

        if (stepCount == 0) {
            return Optional.empty();
        }

        StepCount.Builder builder = new StepCount.Builder(stepCount);

        // this property isn't listed in the table, but does appear in the second Example section where detail is true

        LocalDateTime localDate = asRequiredLocalDateTime(summaryNode, "startTime");
        OffsetDateTime startDateTime=localDate.atOffset(UTC);
        //LocalDate localDate2=localDate.toLocalDate();
        // FIXME fix the time zone offset once Microsoft add it to the API
        //OffsetDateTime startDateTime = localDate2.atStartOfDay().atOffset(UTC);

        StepCount measure = builder.build();

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, null, null));
    }

}
