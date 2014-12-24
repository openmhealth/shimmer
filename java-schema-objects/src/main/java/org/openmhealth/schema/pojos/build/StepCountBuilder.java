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
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;


/**
 * @author Danilo Bonilla
 */
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
        stepCount.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, value, unit));
        return this;
    }

    public StepCountBuilder withStartAndEnd(DateTime start, DateTime end) {
        stepCount.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start, end));
        return this;
    }

    @Override
    public StepCount build() {
        return stepCount;
    }
}
