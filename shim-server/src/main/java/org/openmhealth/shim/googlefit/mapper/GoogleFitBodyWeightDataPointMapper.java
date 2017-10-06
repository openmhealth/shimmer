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
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnitValue;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged weight" endpoint responses
 * (derived:com.google.weight:com.google.android.gms:merge_weight) to {@link BodyWeight}
 * objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitBodyWeightDataPointMapper extends GoogleFitDataPointMapper<BodyWeight> {

    @Override
    public Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode listNode) {

        JsonNode valueList = asRequiredNode(listNode, getValueListNodeName());

        Double bodyWeightValue = asRequiredDouble(valueList.get(0), "fpVal");
        if (bodyWeightValue == 0) {
            return Optional.empty();
        }

        BodyWeight.Builder measureBuilder = new BodyWeight.Builder(new MassUnitValue(KILOGRAM, bodyWeightValue));

        getOptionalTimeFrame(listNode).ifPresent(measureBuilder::setEffectiveTimeFrame);

        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        BodyWeight bodyWeight = measureBuilder.build();
        return Optional.of(newDataPoint(bodyWeight, originDataSourceId.orElse(null)));
    }
}
