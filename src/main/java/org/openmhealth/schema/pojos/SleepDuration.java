package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

public class SleepDuration {

    @JsonProperty(value = "effective-time")
    private TimeFrame effectiveTime;

    public SleepDuration() {
    }

    public TimeFrame getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(TimeFrame effectiveTime) {
        this.effectiveTime = effectiveTime;
    }
}
