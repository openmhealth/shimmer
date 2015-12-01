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
import org.openmhealth.schema.domain.omh.DurationUnit;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount;

import java.math.BigDecimal;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper that translates responses from the iHealth <code>/activity.json</code> endpoint into {@link StepCount}
 * measures.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofActivityReport.htm">endpoint
 * documentation</a>
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

        BigDecimal steps = asRequiredBigDecimal(listEntryNode, "Steps");

        if (steps.intValue() == 0) {
            return Optional.empty();
        }

        StepCount.Builder stepCountBuilder = new StepCount.Builder(steps);

        Optional<Long> dateTimeString = asOptionalLong(listEntryNode, "MDate");

        if (dateTimeString.isPresent()) {

            Optional<String> timeZone = asOptionalString(listEntryNode, "TimeZone");

            if (timeZone.isPresent()) {

                /* iHealth provides daily summaries for step counts and timestamp the datapoint at either the end of
                the day (23:50) or at the latest time that datapoint was synced */
                stepCountBuilder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(
                        getDateTimeAtStartOfDayWithCorrectOffset(dateTimeString.get(), timeZone.get()),
                        new DurationUnitValue(DurationUnit.DAY, 1)));
            }
        }

        setUserNoteIfExists(listEntryNode, stepCountBuilder);

        StepCount stepCount = stepCountBuilder.build();

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, stepCount), stepCount));
    }


}
