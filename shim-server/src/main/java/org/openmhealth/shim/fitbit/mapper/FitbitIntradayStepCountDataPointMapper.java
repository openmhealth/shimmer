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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredBigDecimal;


/**
 * @author Chris Schaefbauer
 */
public class FitbitIntradayStepCountDataPointMapper extends FitbitIntradayDataPointMapper<StepCount> {

    @Override
    protected Optional<DataPoint<StepCount>> asDataPoint(JsonNode listEntryNode) {

        BigDecimal stepCountValue = asRequiredBigDecimal(listEntryNode, "value");

        if (stepCountValue.intValue() == 0) {
            return Optional.empty();
        }

        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepCountValue);

        // We use 1 minute because the Fitbit shim requests 1 minute granularity currently
        Optional<LocalDate> dateFromParent = getDateFromParentNode();

        if (dateFromParent.isPresent()) {

            // Set the effective time frame only if we have access to the date and time
            asOptionalString(listEntryNode, "time").ifPresent(time -> stepCountBuilder
                    .setEffectiveTimeFrame(ofStartDateTimeAndDuration(
                            dateFromParent.get().atTime(LocalTime.parse(time)).atOffset(UTC),
                            new DurationUnitValue(MINUTE, 1))));
        }

        return Optional.of(newDataPoint(stepCountBuilder.build(), null));
    }

    @Override
    protected String getListNodeName() {
        return "activities-steps-intraday.dataset";
    }

    @Override
    public String getDateTimeNodeListName() {
        return "activities-steps";
    }
}
