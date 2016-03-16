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
import org.openmhealth.schema.domain.omh.KcalUnitValue;
import org.openmhealth.schema.domain.omh.PhysicalActivity;

import java.time.ZoneOffset;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper that translates responses from the iHealth <code>/sport.json</code> endpoint into {@link PhysicalActivity}
 * measures.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofSport.htm">endpoint
 * documentation</a>
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
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode listEntryNode,
            Integer measureUnitMagicNumber) {

        String activityName = asRequiredString(listEntryNode, "SportName");

        if (activityName.isEmpty()) {

            return Optional.empty();
        }

        PhysicalActivity.Builder physicalActivityBuilder = new PhysicalActivity.Builder(activityName);

        Optional<Long> startTimeUnixEpochSecs = asOptionalLong(listEntryNode, "SportStartTime");
        Optional<Long> endTimeUnixEpochSecs = asOptionalLong(listEntryNode, "SportEndTime");
        Optional<Integer> timeZoneOffset = asOptionalInteger(listEntryNode, "TimeZone");

        if (startTimeUnixEpochSecs.isPresent() && endTimeUnixEpochSecs.isPresent() && timeZoneOffset.isPresent()) {

            Integer timeZoneOffsetValue = timeZoneOffset.get();
            String timeZoneString = timeZoneOffsetValue.toString();

            // Zone offset cannot parse a positive string offset that's missing a '+' sign (i.e., "0200" vs "+0200")
            if (timeZoneOffsetValue >= 0) {
                timeZoneString = "+" + timeZoneOffsetValue.toString();
            }

            physicalActivityBuilder.setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(
                    getDateTimeWithCorrectOffset(startTimeUnixEpochSecs.get(), ZoneOffset.of(timeZoneString)),
                    getDateTimeWithCorrectOffset(endTimeUnixEpochSecs.get(), ZoneOffset.of(timeZoneString))));
        }

        asOptionalDouble(listEntryNode, "Calories").ifPresent(
                calories -> physicalActivityBuilder.setCaloriesBurned(new KcalUnitValue(KILOCALORIE, calories)));

        PhysicalActivity physicalActivity = physicalActivityBuilder.build();

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, physicalActivity), physicalActivity));
    }
}
