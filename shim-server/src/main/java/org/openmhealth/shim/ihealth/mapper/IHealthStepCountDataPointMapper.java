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

package org.openmhealth.shim.ihealth.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.*;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * @author Chris Schaefbauer
 */
public class IHealthStepCountDataPointMapper extends IHealthDataPointMapper<StepCount> {

    @Override
    protected String getListNodeName() {
        return "ARDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.empty();
    }

    @Override
    protected Optional<DataPoint<StepCount>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        Double steps = asRequiredDouble(listEntryNode, "Steps");

        if (steps == 0) {
            return Optional.empty();
        }

        StepCount.Builder stepCountBuilder = new StepCount.Builder(steps);

        Optional<Long> dateTimeString = asOptionalLong(listEntryNode, "MDate");

        if (dateTimeString.isPresent()) {

            Optional<String> timeZone = asOptionalString(listEntryNode, "TimeZone");

            if (timeZone.isPresent()) {

                OffsetDateTime startDateTime =
                        OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateTimeString.get()), ZoneId.of("Z"))
                                .toLocalDate().atStartOfDay().atOffset(ZoneOffset.of(timeZone.get()));

                stepCountBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(startDateTime,
                        new DurationUnitValue(DurationUnit.DAY, 1)));
            }
        }

        setUserNoteIfExists(listEntryNode, stepCountBuilder);

        StepCount stepCount = stepCountBuilder.build();

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, stepCount), stepCount));
    }


}
