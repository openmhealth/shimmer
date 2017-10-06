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
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.schema.domain.omh.Measure;

import java.math.BigDecimal;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.BODY_WEIGHT;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyWeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyWeight> {

    @Override
    public Optional<Measure.Builder<BodyWeight, ?>> newMeasureBuilder(JsonNode measuresNode) {

        Optional<BigDecimal> value = getValueForMeasureType(measuresNode, BODY_WEIGHT);

        return value.map(weightInKg -> new BodyWeight.Builder(new MassUnitValue(KILOGRAM, weightInKg)));
    }
}
