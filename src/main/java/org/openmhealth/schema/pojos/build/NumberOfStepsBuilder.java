package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.NumberOfSteps;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

public class NumberOfStepsBuilder implements SchemaPojoBuilder<NumberOfSteps> {

    private NumberOfSteps numberOfSteps;

    public NumberOfStepsBuilder() {
        numberOfSteps = new NumberOfSteps();
        numberOfSteps.setEffectiveTimeFrame(new TimeFrame());
    }

    public NumberOfStepsBuilder setSteps(Integer value) {
        numberOfSteps.setValue(value);
        return this;
    }

    public NumberOfStepsBuilder setDuration(String value, String unit) {
        DurationUnitValue durationUnitValue = new DurationUnitValue();
        durationUnitValue.setValue(new BigDecimal(value));
        durationUnitValue.setUnit(DurationUnitValue.DurationUnit.valueOf(unit));
        numberOfSteps.getEffectiveTimeFrame().setDuration(durationUnitValue);
        return this;
    }

    public NumberOfStepsBuilder setStartTime(DateTime dateTime) {
        numberOfSteps.getEffectiveTimeFrame().setStartTime(dateTime);
        return this;
    }

    public NumberOfStepsBuilder setEndTime(DateTime dateTime) {
        numberOfSteps.getEffectiveTimeFrame().setEndTime(dateTime);
        return this;
    }

    @Override
    public NumberOfSteps build() {
        return numberOfSteps;
    }
}
