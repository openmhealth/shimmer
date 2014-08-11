package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.BloodPressure;
import org.openmhealth.schema.pojos.BloodPressureUnit;
import org.openmhealth.schema.pojos.DiastolicBloodPressure;
import org.openmhealth.schema.pojos.SystolicBloodPressure;
import org.openmhealth.schema.pojos.generic.NumericDescriptor;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

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

    public BloodPressureBuilder setPositionDuringMeasurement(String position) {
        bloodPressure.setPosition(BloodPressure.Position.valueOf(position));
        return this;
    }

    public BloodPressureBuilder setNumericDescriptor(String numericDescriptor) {
        bloodPressure.setNumericDescriptor(NumericDescriptor.valueOf(numericDescriptor));
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
        bloodPressure.getEffectiveTimeFrame().setStartTime(dateTime);
        return this;
    }
}
