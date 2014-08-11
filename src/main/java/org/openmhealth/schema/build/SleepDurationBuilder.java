package org.openmhealth.schema.build;

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

    public SleepDurationBuilder setDate(DateTime dateTime) {
        sleepDuration.getEffectiveTime().setStartTime(dateTime);
        return this;
    }

    public SleepDurationBuilder setDuration(String value, String unit) {
        DurationUnitValue durationUnitValue = new DurationUnitValue();
        durationUnitValue.setValue(new BigDecimal(value));
        durationUnitValue.setUnit(DurationUnitValue.DurationUnit.valueOf(unit));
        sleepDuration.getEffectiveTime().setDuration(durationUnitValue);
        return this;
    }

    @Override
    public SleepDuration build() {
        return sleepDuration;
    }
}
