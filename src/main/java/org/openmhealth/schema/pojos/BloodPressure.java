package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.openmhealth.schema.pojos.generic.NumericDescriptor;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = "blood-pressure", namespace = "omh:normalized")
public class BloodPressure {

    @JsonProperty(value = "systolic-blood-pressure", required = false)
    private SystolicBloodPressure systolic;

    @JsonProperty(value = "diastolic-blood-pressure", required = false)
    private DiastolicBloodPressure diastolic;

    @JsonProperty(value = "effective-time-frame", required = false)
    private TimeFrame effectiveTimeFrame;

    @JsonProperty(value = "position-during-measurement", required = false)
    private Position position;

    @JsonProperty(value = "numeric-descriptor", required = false)
    private NumericDescriptor numericDescriptor;

    @JsonProperty(value = "notes", required = false)
    private String notes;

    public enum Position {sitting, lying_down, standing}

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public SystolicBloodPressure getSystolic() {
        return systolic;
    }

    public void setSystolic(SystolicBloodPressure systolic) {
        this.systolic = systolic;
    }

    public DiastolicBloodPressure getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(DiastolicBloodPressure diastolic) {
        this.diastolic = diastolic;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public NumericDescriptor getNumericDescriptor() {
        return numericDescriptor;
    }

    public void setNumericDescriptor(NumericDescriptor numericDescriptor) {
        this.numericDescriptor = numericDescriptor;
    }
}
