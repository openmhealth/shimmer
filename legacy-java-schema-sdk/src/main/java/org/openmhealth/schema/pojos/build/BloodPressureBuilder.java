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
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;


/**
 * @author Danilo Bonilla
 */
public class BloodPressureBuilder implements SchemaPojoBuilder<BloodPressure> {

    private BloodPressure bloodPressure;

    public BloodPressureBuilder() {
        bloodPressure = new BloodPressure();
        bloodPressure.setEffectiveTimeFrame(new TimeFrame());
    }

    public BloodPressureBuilder setValues(BigDecimal systolic, BigDecimal diastolic) {
        SystolicBloodPressure systolicBloodPressure = new SystolicBloodPressure();
        systolicBloodPressure.setValue(systolic);
        systolicBloodPressure.setUnit(BloodPressureUnit.mmHg);
        DiastolicBloodPressure diastolicBloodPressure = new DiastolicBloodPressure();
        diastolicBloodPressure.setValue(diastolic);
        diastolicBloodPressure.setUnit(BloodPressureUnit.mmHg);
        bloodPressure.setDiastolic(diastolicBloodPressure);
        bloodPressure.setSystolic(systolicBloodPressure);
        return this;
    }

    public BloodPressureBuilder setPositionDuringMeasurement(
        PositionDuringMeasurement positionDuringMeasurement) {

        bloodPressure.setPositionDuringMeasurement(positionDuringMeasurement);
        return this;
    }

    public BloodPressureBuilder setDescriptiveStatistic(DescriptiveStatistic descriptiveStatistic) {
        bloodPressure.setDescriptiveStatistic(descriptiveStatistic);
        return this;
    }

    public BloodPressureBuilder setNotes(String notes) {
        bloodPressure.setNotes(notes);
        return this;
    }

    @Override
    public BloodPressure build() {
        return bloodPressure;
    }

    public BloodPressureBuilder setTimeTaken(DateTime dateTime) {
        bloodPressure.setEffectiveTimeFrame(TimeFrame.withDateTime(dateTime));
        return this;
    }
}
