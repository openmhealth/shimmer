package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmhealth.schema.pojos.generic.NumericDescriptor;
import org.openmhealth.schema.pojos.generic.TimeFrame;

public class BloodGlucose {

    @JsonProperty(value = "value", required = true)
    private Double value;

    @JsonProperty(value = "unit", required = true)
    private Unit unit;

    @JsonProperty(value = "effective-time-frame", required = false)
    private TimeFrame effectiveTimeFrame;

    @JsonProperty(value = "numeric-descriptor", required = false)
    private NumericDescriptor numericDescriptor;

    @JsonProperty(value = "notes", required = false)
    private String notes;

    @JsonProperty(value = "meal-context", required = false)
    private MealContext mealContext;

    @JsonProperty(value = "measure-context", required = false)
    private MeasureContext measureContext;

    private enum MealContext {fasting, not_fasting, before_meal, after_meal}

    private enum MeasureContext {whole_blood, plasma}

    private enum Unit {mgdL, mmolL}

    public BloodGlucose() {
    }

    public BloodGlucose(Double value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public NumericDescriptor getNumericDescriptor() {
        return numericDescriptor;
    }

    public void setNumericDescriptor(NumericDescriptor numericDescriptor) {
        this.numericDescriptor = numericDescriptor;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public MealContext getMealContext() {
        return mealContext;
    }

    public void setMealContext(MealContext mealContext) {
        this.mealContext = mealContext;
    }

    public MeasureContext getMeasureContext() {
        return measureContext;
    }

    public void setMeasureContext(MeasureContext measureContext) {
        this.measureContext = measureContext;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
