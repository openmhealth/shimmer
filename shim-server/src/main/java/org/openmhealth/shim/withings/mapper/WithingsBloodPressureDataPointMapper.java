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
import org.openmhealth.schema.domain.omh.BloodPressure;
import org.openmhealth.schema.domain.omh.DiastolicBloodPressure;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.SystolicBloodPressure;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.BloodPressureUnit.MM_OF_MERCURY;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.SYSTOLIC_BLOOD_PRESSURE;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBloodPressureDataPointMapper extends WithingsBodyMeasureDataPointMapper<BloodPressure> {

    private static final Logger logger = getLogger(WithingsBloodPressureDataPointMapper.class);

    @Override
    public Optional<Measure.Builder<BloodPressure, ?>> newMeasureBuilder(JsonNode measuresNode) {

        Optional<BigDecimal> systolicValue = getValueForMeasureType(measuresNode, SYSTOLIC_BLOOD_PRESSURE);
        Optional<BigDecimal> diastolicValue = getValueForMeasureType(measuresNode, DIASTOLIC_BLOOD_PRESSURE);

        if (!systolicValue.isPresent() && !diastolicValue.isPresent()) {
            return empty();
        }

        if (!systolicValue.isPresent() || !diastolicValue.isPresent()) {
            logger.warn("The Withings measure node {} doesn't contain both systolic and diastolic measures.",
                    measuresNode);
            return empty();
        }

        return Optional.of(new BloodPressure.Builder(
                new SystolicBloodPressure(MM_OF_MERCURY, systolicValue.get()),
                new DiastolicBloodPressure(MM_OF_MERCURY, diastolicValue.get())
        ));
    }
}
