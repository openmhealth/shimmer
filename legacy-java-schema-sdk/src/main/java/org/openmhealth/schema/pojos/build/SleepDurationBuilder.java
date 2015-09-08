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
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.SleepDurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;


/**
 * @author Danilo Bonilla
 */
public class SleepDurationBuilder implements SchemaPojoBuilder<SleepDuration> {

    private SleepDuration sleepDuration;

    public SleepDurationBuilder() {
        sleepDuration = new SleepDuration();
    }

    public SleepDurationBuilder withStartAndEndAndDuration(DateTime start,
                                                           DateTime end,
                                                           Double value,
                                                           SleepDurationUnitValue.Unit unit
    ) {
        sleepDuration.setEffectiveTimeFrame(
            TimeFrame.withTimeInterval(start,end)
        );
        sleepDuration.setSleepDurationUnitValue(
            new SleepDurationUnitValue(new BigDecimal(value), unit));
        return this;
    }

    public SleepDurationBuilder withStartAndEnd(DateTime start, DateTime end) {
        sleepDuration.setEffectiveTimeFrame(TimeFrame.withTimeInterval(start,end));
        return this;
    }

    public SleepDurationBuilder withStartAndDuration(DateTime start, Double value, SleepDurationUnitValue.Unit unit) {
        sleepDuration.setEffectiveTimeFrame(TimeFrame.withDateTime(start));
        sleepDuration.setSleepDurationUnitValue(new SleepDurationUnitValue(new BigDecimal(value), unit));
        return this;
    }

    public SleepDurationBuilder setNotes(String notes) {
        sleepDuration.setNotes(notes);
        return this;
    }

    @Override
    public SleepDuration build() {
        return sleepDuration;
    }
}
