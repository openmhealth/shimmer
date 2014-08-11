package org.openmhealth.schema.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

public class ActivityBuilder implements SchemaPojoBuilder<Activity> {

    private Activity activity;

    @Override
    public Activity build() {
        return activity;
    }

    public ActivityBuilder() {
        activity = new Activity();
        activity.setEffectiveTimeFrame(new TimeFrame());
    }

    public ActivityBuilder setActivityName(String activityName) {
        activity.setActivityName(activityName);
        return this;
    }

    public ActivityBuilder setDistance(String value, String unit) {
        LengthUnitValue distance = new LengthUnitValue();
        distance.setValue(new BigDecimal(value));
        distance.setUnit(LengthUnitValue.LengthUnit.valueOf(unit));
        activity.setDistance(distance);
        return this;
    }

    public ActivityBuilder setDuration(String value, String unit) {
        DurationUnitValue durationUnitValue = new DurationUnitValue();
        durationUnitValue.setValue(new BigDecimal(value));
        durationUnitValue.setUnit(DurationUnitValue.DurationUnit.valueOf(unit));
        activity.getEffectiveTimeFrame().setDuration(durationUnitValue);
        return this;
    }

    public ActivityBuilder setStartTime(DateTime dateTime) {
        activity.getEffectiveTimeFrame().setStartTime(dateTime);
        return this;
    }

    public ActivityBuilder setEndTime(DateTime dateTime) {
        activity.getEffectiveTimeFrame().setEndTime(dateTime);
        return this;
    }
}
