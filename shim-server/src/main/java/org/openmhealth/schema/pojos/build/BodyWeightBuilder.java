/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

/**
 * @author Danilo Bonilla
 */
public class BodyWeightBuilder implements SchemaPojoBuilder<BodyWeight> {

    private BodyWeight bodyWeight;

    public BodyWeightBuilder() {
        bodyWeight = new BodyWeight();
        bodyWeight.setEffectiveTimeFrame(new TimeFrame());
    }

    public BodyWeightBuilder setWeight(String value, String unit) {
        MassUnitValue massUnitValue = new MassUnitValue();
        massUnitValue.setValue(new BigDecimal(value));
        massUnitValue.setUnit(MassUnitValue.MassUnit.valueOf(unit));
        bodyWeight.setMassUnitValue(massUnitValue);
        return this;
    }

    public BodyWeightBuilder setWeight(Double value, MassUnitValue.MassUnit unit) {
        MassUnitValue massUnitValue = new MassUnitValue();
        massUnitValue.setValue(new BigDecimal(value));
        massUnitValue.setUnit(unit);
        bodyWeight.setMassUnitValue(massUnitValue);
        return this;
    }

    public BodyWeightBuilder setTimeTaken(DateTime dateTime) {
        bodyWeight.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }

    @Override
    public BodyWeight build() {
        return bodyWeight;
    }
}
