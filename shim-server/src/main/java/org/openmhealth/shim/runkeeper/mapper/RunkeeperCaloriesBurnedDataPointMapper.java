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
import org.openmhealth.schema.domain.omh.CaloriesBurned;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.KcalUnit;
import org.openmhealth.schema.domain.omh.KcalUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.FitnessActivityFeed+json responses to {@link
 * CaloriesBurned} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class RunkeeperCaloriesBurnedDataPointMapper extends RunkeeperDataPointMapper<CaloriesBurned> {


    @Override
    protected Optional<DataPoint<CaloriesBurned>> asDataPoint(JsonNode itemNode) {

        Optional<CaloriesBurned> caloriesBurned = getMeasure(itemNode);

        if (caloriesBurned.isPresent()) {
            return Optional
                    .of(new DataPoint<>(getDataPointHeader(itemNode, caloriesBurned.get()), caloriesBurned.get()));
        }
        else {
            return Optional.empty(); // return empty if there was no calories information to generate a datapoint
        }

    }

    private Optional<CaloriesBurned> getMeasure(JsonNode itemNode) {

        Optional<Double> calorieValue = asOptionalDouble(itemNode, "total_calories");
        if (!calorieValue.isPresent()) {  // Not all activity datapoints have the "total_calories" property
            return Optional.empty();
        }
        CaloriesBurned.Builder caloriesBurnedBuilder =
                new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE, calorieValue.get()));

        setEffectiveTimeFrameIfPresent(itemNode, caloriesBurnedBuilder);

        Optional<String> activityType = asOptionalString(itemNode, "type");
        activityType.ifPresent(at -> caloriesBurnedBuilder.setActivityName(at));

        return Optional.of(caloriesBurnedBuilder.build());

    }
}
