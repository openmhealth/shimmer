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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.CaloriesBurned2;
import org.openmhealth.schema.domain.omh.DataPoint;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Withings Activity Measures endpoint responses (/measure?action=getactivity) to {@link CaloriesBurned2}
 * objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_activity">Activity Measures API documentation</a>
 */
public class WithingsDailyCaloriesBurnedDataPointMapper extends WithingsListDataPointMapper<CaloriesBurned2> {

    @Override
    String getListNodeName() {
        return "activities";
    }

    /**
     * Maps an individual list node from the array in the Withings activity measure endpoint response into a {@link
     * CaloriesBurned2} data point.
     *
     * @param node activity node from the array "activites" contained in the "body" of the endpoint response
     */
    @Override
    public Optional<DataPoint<CaloriesBurned2>> asDataPoint(JsonNode node) {

        long caloriesBurnedValue = asRequiredLong(node, "calories");

        CaloriesBurned2.Builder caloriesBurnedBuilder =
                new CaloriesBurned2.Builder(KILOCALORIE.newUnitValue(caloriesBurnedValue), getTimeFrame(node));

        asOptionalString(node, "comment").ifPresent(caloriesBurnedBuilder::setUserNotes);

        CaloriesBurned2 caloriesBurned = caloriesBurnedBuilder.build();

        return Optional.of(newDataPoint(caloriesBurned, asRequiredString(node, "date"), true, null));
    }
}
