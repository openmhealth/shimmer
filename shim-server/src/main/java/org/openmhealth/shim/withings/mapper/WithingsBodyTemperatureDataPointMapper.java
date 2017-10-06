/*
 * Copyright 2017 Open mHealth
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
import org.openmhealth.schema.domain.omh.BodyTemperature;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.TemperatureUnitValue;

import java.math.BigDecimal;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.TemperatureUnit.CELSIUS;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.BODY_TEMPERATURE;


/**
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyTemperatureDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyTemperature> {

    // TODO since the documentation doesn't explicitly say the temperature is in C, this needs testing
    @Override
    public Optional<Measure.Builder<BodyTemperature, ?>> newMeasureBuilder(JsonNode measuresNode) {

        Optional<BigDecimal> value = getValueForMeasureType(measuresNode, BODY_TEMPERATURE);

        return value
                .map(temperatureInC -> new BodyTemperature.Builder(new TemperatureUnitValue(CELSIUS, temperatureInC)));
    }
}
