package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

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
