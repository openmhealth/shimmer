package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;
import org.openmhealth.schema.pojos.generic.TimeInterval;

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

    public ActivityBuilder setDistance(Double value, LengthUnitValue.LengthUnit unit) {
        LengthUnitValue distance = new LengthUnitValue();
        distance.setValue(new BigDecimal(value));
        distance.setUnit(unit);
        activity.setDistance(distance);
        return this;
    }

    public ActivityBuilder setReportedActivityIntensity(Activity.ActivityIntensity activityIntensity) {
        activity.setReportedActivityIntensity(activityIntensity);
        return this;
    }

    public ActivityBuilder withStartAndDuration(DateTime start, Double value,
                                                DurationUnitValue.DurationUnit unit) {
        activity.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, value, unit));
        return this;
    }

    public ActivityBuilder withStartAndEnd(DateTime start, DateTime end) {
        activity.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, end));
        return this;
    }
}
