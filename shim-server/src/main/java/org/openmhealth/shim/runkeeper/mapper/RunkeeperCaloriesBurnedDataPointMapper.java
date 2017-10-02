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

package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.CaloriesBurned2;
import org.openmhealth.schema.domain.omh.DataPoint;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.FitnessActivityFeed+json responses to {@link
 * CaloriesBurned2} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class RunkeeperCaloriesBurnedDataPointMapper extends RunkeeperDataPointMapper<CaloriesBurned2> {

    @Override
    protected Optional<DataPoint<CaloriesBurned2>> asDataPoint(JsonNode itemNode) {

        Optional<CaloriesBurned2> caloriesBurned = newMeasure(itemNode);

        // return empty if there was no calories information to generate a data point
        return caloriesBurned
                .map(measure -> new DataPoint<>(getDataPointHeader(itemNode, measure), measure));
    }

    private Optional<CaloriesBurned2> newMeasure(JsonNode itemNode) {

        Optional<Double> calorieValue = asOptionalDouble(itemNode, "total_calories");

        if (!calorieValue.isPresent()) {  // not all activity data points have the "total_calories" property
            return Optional.empty();
        }

        CaloriesBurned2.Builder caloriesBurnedBuilder =
                new CaloriesBurned2.Builder(KILOCALORIE.newUnitValue(calorieValue.get()), getTimeFrame(itemNode));

        asOptionalString(itemNode, "type").ifPresent(caloriesBurnedBuilder::setActivityName);

        return Optional.of(caloriesBurnedBuilder.build());
    }
}
