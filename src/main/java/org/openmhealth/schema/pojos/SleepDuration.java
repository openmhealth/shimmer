package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = "sleep-duration", namespace = "omh:normalized")
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
