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
import org.openmhealth.schema.pojos.BodyHeight;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

/**
 * @author Danilo Bonilla
 */
public class BodyHeightBuilder implements SchemaPojoBuilder<BodyHeight> {

    private BodyHeight bodyHeight;

    public BodyHeightBuilder() {
        bodyHeight = new BodyHeight();
        bodyHeight.setEffectiveTimeFrame(new TimeFrame());
    }

    public BodyHeightBuilder setHeight(String value, String unit) {
        LengthUnitValue lengthUnitValue = new LengthUnitValue();
        lengthUnitValue.setValue(new BigDecimal(value));
        lengthUnitValue.setUnit(LengthUnitValue.LengthUnit.valueOf(unit));
        bodyHeight.setLengthUnitValue(lengthUnitValue);
        return this;
    }

    public BodyHeightBuilder setHeight(Double value, LengthUnitValue.LengthUnit unit) {
        LengthUnitValue lengthUnitValue = new LengthUnitValue();
        lengthUnitValue.setValue(new BigDecimal(value));
        lengthUnitValue.setUnit(unit);
        bodyHeight.setLengthUnitValue(lengthUnitValue);
        return this;
    }

    public BodyHeightBuilder setTimeTaken(DateTime dateTime) {
        bodyHeight.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }

    @Override
    public BodyHeight build() {
        return bodyHeight;
    }
}
