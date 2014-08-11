package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

public class BodyHeight {

    @JsonProperty(value = "length-unit-value", required = true)
    private LengthUnitValue lengthUnitValue;

    @JsonProperty(value = "effective-time-frame", required = true)
    private TimeFrame effectiveTimeFrame;

    public BodyHeight() {
    }

    public BodyHeight(LengthUnitValue lengthUnitValue, TimeFrame effectiveTimeFrame) {
        this.lengthUnitValue = lengthUnitValue;
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public LengthUnitValue getLengthUnitValue() {
        return lengthUnitValue;
    }

    public void setLengthUnitValue(LengthUnitValue lengthUnitValue) {
        this.lengthUnitValue = lengthUnitValue;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
}
