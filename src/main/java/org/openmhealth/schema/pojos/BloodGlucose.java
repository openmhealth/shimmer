package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.NumericDescriptor;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = BloodGlucose.SCHEMA_BLOOD_GLUCOSE, namespace = DataPoint.NAMESPACE)
public class BloodGlucose extends BaseDataPoint {

    @JsonProperty(value = "value", required = true)
    private BigDecimal value;

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

    public enum MealContext {fasting, not_fasting, before_meal, after_meal}

    public enum MeasureContext {whole_blood, plasma}

    public enum Unit {mgdL, mmolL}

    public static final String SCHEMA_BLOOD_GLUCOSE = "blood-glucose";

    public BloodGlucose() {
    }

    public BloodGlucose(BigDecimal value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_BLOOD_GLUCOSE;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getStartTime();
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

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
