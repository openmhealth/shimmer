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

package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.CaloriesBurned2;
import org.openmhealth.schema.domain.omh.DataPoint;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged calories expended" endpoint responses (derived:com.google.calories.expended:com.google.android.gms:merge_calories_expended)
 * to {@link CaloriesBurned2} objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitCaloriesBurnedDataPointMapper extends GoogleFitDataPointMapper<CaloriesBurned2> {

    @Override
    protected Optional<DataPoint<CaloriesBurned2>> asDataPoint(JsonNode listNode) {

        JsonNode listValueNode = asRequiredNode(listNode, "value");
        double caloriesBurnedValue = asRequiredDouble(listValueNode.get(0), "fpVal");

        CaloriesBurned2.Builder measureBuilder =
                new CaloriesBurned2.Builder(KILOCALORIE.newUnitValue(caloriesBurnedValue), getTimeFrame(listNode));

        CaloriesBurned2 caloriesBurned = measureBuilder.build();

        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        // Google Fit calories burned endpoint returns calories burned by basal metabolic rate (BMR), however these
        // are not activity related calories burned so we do not create a data point for values from this source
        if (originDataSourceId.isPresent()) {
            if (originDataSourceId.get().contains("bmr")) {
                return Optional.empty();
            }
        }

        return Optional.of(newDataPoint(caloriesBurned, originDataSourceId.orElse(null)));
    }
}
