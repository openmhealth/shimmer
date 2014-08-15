package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = BodyWeight.SCHEMA_BODY_WEIGHT, namespace = "omh:normalized")
public class BodyWeight implements DataPoint{

    @JsonProperty(value = "mass-unit-value", required = true)
    private MassUnitValue massUnitValue;

    @JsonProperty(value = "effective-time-frame", required = true)
    private TimeFrame effectiveTimeFrame;

    public static final String SCHEMA_BODY_WEIGHT = "body-weight";

    public BodyWeight() {
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_BODY_WEIGHT;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getStartTime();
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
