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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount2;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.math.BigDecimal;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper that translates responses from the iHealth <code>/activity.json</code> endpoint into {@link StepCount2}
 * measures.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofActivityReport.htm">endpoint
 * documentation</a>
 */
public class IHealthStepCountDataPointMapper extends IHealthDataPointMapper<StepCount2> {

    @Override
    protected String getListNodeName() {
        return "ARDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.empty();
    }

    @Override
    protected Optional<DataPoint<StepCount2>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        BigDecimal steps = asRequiredBigDecimal(listEntryNode, "Steps");

        if (steps.intValue() == 0) {
            return Optional.empty();
        }

        Long effectiveEpochSecondsInLocalTime = asRequiredLong(listEntryNode, "MDate");
        String effectiveTimeZoneOffset = asRequiredString(listEntryNode, "TimeZone");

        /* iHealth provides daily summaries for step counts and timestamp the data point at either the end of
           the day (23:50) or at the latest time that data point was synced. */
        TimeInterval effectiveTimeInterval = ofStartDateTimeAndDuration(
                getDateTimeAtStartOfDayWithCorrectOffset(effectiveEpochSecondsInLocalTime, effectiveTimeZoneOffset),
                new DurationUnitValue(DAY, 1));

        StepCount2.Builder measureBuilder = new StepCount2.Builder(steps, effectiveTimeInterval);

        getUserNoteIfExists(listEntryNode).ifPresent(measureBuilder::setUserNotes);

        StepCount2 stepCount = measureBuilder.build();

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, stepCount), stepCount));
    }
}
