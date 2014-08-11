package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmhealth.schema.pojos.base.TimeFrame;

public class NumberOfSteps {

    @JsonProperty(value = "value", required = true)
    private Integer value;

    @JsonProperty(value = "effective-time-frame", required = true)
    private TimeFrame effectiveTimeFrame;

    public NumberOfSteps() {
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
}
