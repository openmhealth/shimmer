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

import java.time.ZoneOffset;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.TimeInterval.*;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper that translates responses from the iHealth <code>/sleep.json</code> endpoint into {@link SleepDuration}
 * measures.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofSleepReport.htm">endpoint
 * documentation</a>
 */
public class IHealthSleepDurationDataPointMapper extends IHealthDataPointMapper<SleepDuration> {

    @Override
    protected String getListNodeName() {
        return "SRDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.empty();
    }

    @Override
    protected Optional<DataPoint<SleepDuration>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(
                new DurationUnitValue(DurationUnit.MINUTE, asRequiredBigDecimal(listEntryNode, "HoursSlept")));

        Optional<Long> startTime = asOptionalLong(listEntryNode, "StartTime");
        Optional<Long> endTime = asOptionalLong(listEntryNode, "EndTime");

        if (startTime.isPresent() && endTime.isPresent()) {

            Optional<String> timeZone = asOptionalString(listEntryNode, "TimeZone");

            if (timeZone.isPresent()) {

                sleepDurationBuilder.setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(
                        getDateTimeWithCorrectOffset(startTime.get(), ZoneOffset.of(timeZone.get())),
                        getDateTimeWithCorrectOffset(endTime.get(), ZoneOffset.of(timeZone.get()))));
            }
        }

        setUserNoteIfExists(listEntryNode, sleepDurationBuilder);

        SleepDuration sleepDuration = sleepDurationBuilder.build();
        asOptionalBigDecimal(listEntryNode, "Awaken")
                .ifPresent(awaken -> sleepDuration.setAdditionalProperty("wakeup_count", awaken));

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, sleepDuration), sleepDuration));
    }
}
