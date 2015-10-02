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
import org.openmhealth.schema.domain.omh.PhysicalActivity;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * @author Chris Schaefbauer
 */
public class IHealthPhysicalActivityDataPointMapper extends IHealthDataPointMapper<PhysicalActivity> {

    @Override
    protected String getListNodeName() {
        return "SPORTDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {

        return Optional.empty();
    }

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode listNode, Integer measureUnitMagicNumber) {

        String activityName = asRequiredString(listNode, "SportName");

        if (activityName.isEmpty()) {
            return Optional.empty();
        }

        PhysicalActivity.Builder physicalActivityBuilder = new PhysicalActivity.Builder(activityName);

        Optional<Long> startTimeUnixEpochSecs = asOptionalLong(listNode, "SportStartTime");
        Optional<Long> endTimeUnixEpochSecs = asOptionalLong(listNode, "SportEndTime");
        Optional<Integer> timeZoneOffset = asOptionalInteger(listNode, "TimeZone");

        if (startTimeUnixEpochSecs.isPresent() && endTimeUnixEpochSecs.isPresent() && timeZoneOffset.isPresent()) {

            physicalActivityBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                    OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(startTimeUnixEpochSecs.get()),
                            ZoneId.ofOffset("UTC", ZoneOffset.ofHours(timeZoneOffset.get()))),
                    OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(endTimeUnixEpochSecs.get()),
                            ZoneId.ofOffset("UTC", ZoneOffset.ofHours(timeZoneOffset.get())))));
        }

        PhysicalActivity physicalActivity = physicalActivityBuilder.build();
        return Optional.of(new DataPoint<>(createDataPointHeader(listNode, physicalActivity), physicalActivity));
    }
}
