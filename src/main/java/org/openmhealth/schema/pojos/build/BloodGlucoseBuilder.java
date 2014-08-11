package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.BloodGlucose;
import org.openmhealth.schema.pojos.generic.NumericDescriptor;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

public class BloodGlucoseBuilder implements SchemaPojoBuilder<BloodGlucose> {

    private BloodGlucose bloodGlucose;

    public BloodGlucoseBuilder() {
        bloodGlucose = new BloodGlucose();
        bloodGlucose.setEffectiveTimeFrame(new TimeFrame());
    }

    public BloodGlucoseBuilder setValue(String value) {
        bloodGlucose.setValue(new BigDecimal(value));
        bloodGlucose.setUnit(BloodGlucose.Unit.mgdL);
        return this;
    }

    public BloodGlucoseBuilder setValueAndUnit(String value, String unit) {
        bloodGlucose.setValue(new BigDecimal(value));
        bloodGlucose.setUnit(BloodGlucose.Unit.valueOf(unit));
        return this;
    }

    public BloodGlucoseBuilder setMealContext(String mealContext) {
        if (mealContext != null) {
            bloodGlucose.setMealContext(
                BloodGlucose.MealContext.valueOf(mealContext));
        }
        return this;
    }

    public BloodGlucoseBuilder setMeasureContext(String measureContext) {
        if (measureContext != null) {
            bloodGlucose.setMeasureContext(
                BloodGlucose.MeasureContext.valueOf(measureContext));
        }
        return this;
    }

    public BloodGlucoseBuilder setNumericDescriptor(String numericDescriptor) {
        bloodGlucose.setNumericDescriptor(NumericDescriptor.valueOf(numericDescriptor));
        return this;
    }

    public BloodGlucoseBuilder setNotes(String notes) {
        bloodGlucose.setNotes(notes);
        return this;
    }

    public BloodGlucoseBuilder setTimeTaken(DateTime dateTime) {
        bloodGlucose.getEffectiveTimeFrame().setStartTime(dateTime);
        return this;
    }

    @Override
    public BloodGlucose build() {
        return bloodGlucose;
    }
}
