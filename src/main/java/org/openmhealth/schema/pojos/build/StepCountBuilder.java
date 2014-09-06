package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;
import org.openmhealth.schema.pojos.generic.TimeInterval;

public class StepCountBuilder implements SchemaPojoBuilder<StepCount> {

    private StepCount stepCount;

    public StepCountBuilder() {
        stepCount = new StepCount();
    }

    public StepCountBuilder setSteps(Integer value) {
        stepCount.setStepCount(value);
        return this;
    }

    public StepCountBuilder withStartAndDuration(DateTime start, Double value,
                                                 DurationUnitValue.DurationUnit unit) {
        stepCount.setEffectiveTimeFrame(TimeInterval.withStartAndDuration(start, value, unit));
        return this;
    }

    public StepCountBuilder withStartAndEnd(DateTime start, DateTime end) {
        stepCount.setEffectiveTimeFrame(TimeInterval.withStartAndEnd(start, end));
        return this;
    }

    @Override
    public StepCount build() {
        return stepCount;
    }
}
