package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.SleepDurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeInterval;

import java.math.BigDecimal;

public class SleepDurationBuilder implements SchemaPojoBuilder<SleepDuration> {

    private SleepDuration sleepDuration;

    public SleepDurationBuilder() {
        sleepDuration = new SleepDuration();
    }

    public SleepDurationBuilder withStartAndEndAndDuration(DateTime start,
                                                           DateTime end,
                                                           Double value,                                                           SleepDurationUnitValue.Unit unit) {
        sleepDuration.setEffectiveTimeFrame(
            TimeInterval.withStartAndEnd(start, end)
        );
        sleepDuration.setSleepDurationUnitValue(
            new SleepDurationUnitValue(new BigDecimal(value), unit));
        return this;
    }

    public SleepDurationBuilder setNotes(String notes){
        sleepDuration.setNotes(notes);
        return this;
    }

    @Override
    public SleepDuration build() {
        return sleepDuration;
    }
}
