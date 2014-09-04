package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.BloodGlucose;
import org.openmhealth.schema.pojos.BloodGlucoseUnitValue;
import org.openmhealth.schema.pojos.BloodSpecimenType;
import org.openmhealth.schema.pojos.TemporalRelationshipToMeal;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

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
