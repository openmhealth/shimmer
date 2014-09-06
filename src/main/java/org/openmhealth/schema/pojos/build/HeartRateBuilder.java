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
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.HeartRateUnitValue;
import org.openmhealth.schema.pojos.TemporalRelationshipToPhysicalActivity;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import static org.openmhealth.schema.pojos.HeartRateUnitValue.Unit.bpm;

/**
 * @author Danilo Bonilla
 */
public class HeartRateBuilder implements SchemaPojoBuilder<HeartRate> {

    private HeartRate heartRate;

    public HeartRateBuilder() {
        heartRate = new HeartRate();
        heartRate.setEffectiveTimeFrame(new TimeFrame());
    }

    public HeartRateBuilder withTimeTaken(DateTime dateTime) {
        heartRate.getEffectiveTimeFrame().setDateTime(dateTime);
        return this;
    }

    public HeartRateBuilder withRate(Integer value) {
        HeartRateUnitValue heartRateUnitValue = new HeartRateUnitValue(value, bpm);
        heartRate.setHeartRate(heartRateUnitValue);
        return this;
    }

    public HeartRateBuilder withTimeTakenDescription(
        TemporalRelationshipToPhysicalActivity description) {
        heartRate.setTemporalRelationshipToPhysicalActivity(description);
        return this;
    }

    @Override
    public HeartRate build() {
        return heartRate;
    }
}
