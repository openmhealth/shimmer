package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

public class NumberOfStepsBuilder implements SchemaPojoBuilder<StepCount> {

    private StepCount stepCount;

    public NumberOfStepsBuilder() {
        stepCount = new StepCount();
        stepCount.setEffectiveTimeFrame(new TimeFrame());
    }

    public NumberOfStepsBuilder setSteps(Integer value) {
        stepCount.setValue(value);
        return this;
    }

    public NumberOfStepsBuilder withStartAndDuration(DateTime start, Double value,
                                                     DurationUnitValue.DurationUnit unit) {
        stepCount.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, value, unit));
        return this;
    }

    public NumberOfStepsBuilder withStartAndEnd(DateTime start, DateTime end) {
        stepCount.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, end));
        return this;
    }

    @Override
    public StepCount build() {
        return stepCount;
    }
}
