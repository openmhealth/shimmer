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
import org.openmhealth.schema.pojos.BloodGlucose;
import org.openmhealth.schema.pojos.BloodGlucoseUnitValue;
import org.openmhealth.schema.pojos.BloodSpecimenType;
import org.openmhealth.schema.pojos.TemporalRelationshipToMeal;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

/**
 * @author Danilo Bonilla
 */
public class BloodGlucoseBuilder implements SchemaPojoBuilder<BloodGlucose> {

    private BloodGlucose bloodGlucose;

    public BloodGlucoseBuilder() {
        bloodGlucose = new BloodGlucose();
        bloodGlucose.setEffectiveTimeFrame(new TimeFrame());
    }

    public BloodGlucoseBuilder setMgdLValue(BigDecimal value) {
        bloodGlucose.setBloodGlucose(
            new BloodGlucoseUnitValue(value, BloodGlucoseUnitValue.Unit.mg_dL));
        return this;
    }

    public BloodGlucoseBuilder setValueAndUnit(
        BigDecimal value, BloodGlucoseUnitValue.Unit unit) {
        bloodGlucose.setBloodGlucose(new BloodGlucoseUnitValue(value, unit));
        return this;
    }

    public BloodGlucoseBuilder setTemporalRelationshipToMeal(
        TemporalRelationshipToMeal mealContext) {
        if (mealContext != null) {
            bloodGlucose.setTemporalRelationshipToMeal(mealContext);
        }
        return this;
    }

    public BloodGlucoseBuilder setBloodSpecimenType(BloodSpecimenType bloodSpecimenType) {
        if (bloodSpecimenType != null) {
            bloodGlucose.setBloodSpecimenType(bloodSpecimenType);
        }
        return this;
    }

    public BloodGlucoseBuilder setDescriptiveStatistic(DescriptiveStatistic numericDescriptor) {
        bloodGlucose.setDescriptiveStatistic(numericDescriptor);
        return this;
    }

    public BloodGlucoseBuilder setNotes(String notes) {
        bloodGlucose.setNotes(notes);
        return this;
    }

    public BloodGlucoseBuilder setTimeTaken(DateTime dateTime) {
        bloodGlucose.getEffectiveTimeFrame().setDateTime(dateTime);
        return this;
    }

    @Override
    public BloodGlucose build() {
        return bloodGlucose;
    }
}
