/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;


/**
 * @author Danilo Bonilla
 */
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
