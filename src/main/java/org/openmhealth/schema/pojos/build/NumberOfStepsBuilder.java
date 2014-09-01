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

    public NumberOfStepsBuilder withStartAndDuration(DateTime start, Double value,
                                                     DurationUnitValue.DurationUnit unit) {
        numberOfSteps.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, value, unit));
        return this;
    }

    public NumberOfStepsBuilder withStartAndEnd(DateTime start, DateTime end) {
        numberOfSteps.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, end));
        return this;
    }

    @Override
    public NumberOfSteps build() {
        return numberOfSteps;
    }
}
