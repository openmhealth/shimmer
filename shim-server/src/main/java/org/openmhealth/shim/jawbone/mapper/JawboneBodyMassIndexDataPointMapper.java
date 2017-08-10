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

package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyMassIndex1;
import org.openmhealth.schema.domain.omh.BodyMassIndexUnit1;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.TypedUnitValue;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.BodyMassIndexUnit1.KILOGRAMS_PER_SQUARE_METER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.jawbone.mapper.JawboneBodyEventType.BODY_MASS_INDEX;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/body">API documentation</a>
 */
public class JawboneBodyMassIndexDataPointMapper extends JawboneBodyEventsDataPointMapper<BodyMassIndex1>{

    @Override
    Optional<Measure.Builder<BodyMassIndex1, ?>> newMeasureBuilder(JsonNode listEntryNode) {

        TypedUnitValue<BodyMassIndexUnit1> bmiValue =
                new TypedUnitValue<>(KILOGRAMS_PER_SQUARE_METER, asRequiredDouble(listEntryNode, "bmi"));

        return Optional.of(new BodyMassIndex1.Builder(bmiValue));
    }

    @Override
    protected JawboneBodyEventType getBodyEventType() {
        return BODY_MASS_INDEX;
    }
}
