package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmhealth.schema.pojos.generic.NumericDescriptor;
import org.openmhealth.schema.pojos.generic.TimeFrame;

public class HeartRate {

    @JsonProperty(value = "value", required = true)
    private Integer value;

    @JsonProperty(value = "unit", required = true)
    private Unit unit;

    @JsonProperty(value = "effective-time-frame", required = false)
    private TimeFrame effectiveTimeFrame;

    @JsonProperty(value = "numeric-descriptor", required = false)
    private NumericDescriptor numericDescriptor;

    @JsonProperty(value = "notes", required = false)
    private String notes;

    public HeartRate() {
    }

    public HeartRate(Integer value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public NumericDescriptor getNumericDescriptor() {
        return numericDescriptor;
    }

    public void setNumericDescriptor(NumericDescriptor numericDescriptor) {
        this.numericDescriptor = numericDescriptor;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public enum Unit {bpm}

}
