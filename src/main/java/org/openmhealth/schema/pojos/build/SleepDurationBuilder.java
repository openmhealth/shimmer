package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

public class SleepDurationBuilder implements SchemaPojoBuilder<SleepDuration> {

    private SleepDuration sleepDuration;

    public SleepDurationBuilder() {
        sleepDuration = new SleepDuration();
        sleepDuration.setEffectiveTime(new TimeFrame());
    }

    public SleepDurationBuilder withStartAndDuration(DateTime start, Double value,
                                                     DurationUnitValue.DurationUnit unit) {
        sleepDuration.setEffectiveTime(TimeFrame.withTimeInterval(start, value, unit));
        return this;
    }

    @Override
    public SleepDuration build() {
        return sleepDuration;
    }
}
