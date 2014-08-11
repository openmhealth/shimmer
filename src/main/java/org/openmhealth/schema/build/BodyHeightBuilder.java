package org.openmhealth.schema.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.BodyHeight;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

public class BodyHeightBuilder implements SchemaPojoBuilder<BodyHeight> {

    private BodyHeight bodyHeight;

    public BodyHeightBuilder() {
        bodyHeight = new BodyHeight();
        bodyHeight.setEffectiveTimeFrame(new TimeFrame());
    }

    public BodyHeightBuilder setWeight(String value, String unit) {
        LengthUnitValue lengthUnitValue = new LengthUnitValue();
        lengthUnitValue.setValue(new BigDecimal(value));
        lengthUnitValue.setUnit(LengthUnitValue.LengthUnit.valueOf(unit));
        bodyHeight.setLengthUnitValue(lengthUnitValue);
        return this;
    }

    public BodyHeightBuilder setTimeTaken(DateTime dateTime) {
        bodyHeight.getEffectiveTimeFrame().setStartTime(dateTime);
        return this;
    }

    @Override
    public BodyHeight build() {
        return bodyHeight;
    }
}
