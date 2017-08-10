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
import org.openmhealth.schema.domain.omh.BodyMassIndex1;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.TypedUnitValue;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.BodyMassIndexUnit1.KILOGRAMS_PER_SQUARE_METER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * A mapper that translates responses from the iHealth <code>/weight.json</code> endpoint into {@link BodyMassIndex1}
 * measures.
 *
 * @author Emerson Farrugia
 * @author Chris Schaefbauer
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofWeight.htm">endpoint
 * documentation</a>
 */
public class IHealthBodyMassIndexDataPointMapper extends IHealthDataPointMapper<BodyMassIndex1> {

    @Override
    protected String getListNodeName() {
        return "WeightDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.of("WeightUnit");
    }

    @Override
    protected Optional<DataPoint<BodyMassIndex1>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        Double bmiValue = asRequiredDouble(listEntryNode, "BMI");

        if (bmiValue == 0) {
            return Optional.empty();
        }

        BodyMassIndex1.Builder bodyMassIndexBuilder =
                new BodyMassIndex1.Builder(new TypedUnitValue<>(KILOGRAMS_PER_SQUARE_METER, bmiValue));

        getEffectiveTimeFrameAsDateTime(listEntryNode).ifPresent(bodyMassIndexBuilder::setEffectiveTimeFrame);

        getUserNoteIfExists(listEntryNode).ifPresent(bodyMassIndexBuilder::setUserNotes);

        BodyMassIndex1 bodyMassIndex = bodyMassIndexBuilder.build();
        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, bodyMassIndex), bodyMassIndex));

    }
}
