package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmhealth.schema.pojos.base.MassUnitValue;
import org.openmhealth.schema.pojos.base.TimeFrame;

public class BodyWeight {

    @JsonProperty(value = "mass-unit-value", required = true)
    private MassUnitValue massUnitValue;

    @JsonProperty(value = "effective-time-frame", required = true)
    private TimeFrame effectiveTimeFrame;

    public BodyWeight() {
    }

    public MassUnitValue getMassUnitValue() {
        return massUnitValue;
    }

    public void setMassUnitValue(MassUnitValue massUnitValue) {
        this.massUnitValue = massUnitValue;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
}
