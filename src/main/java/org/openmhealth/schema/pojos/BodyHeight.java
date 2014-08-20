package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = BodyHeight.SCHEMA_BODY_HEIGHT, namespace = DataPoint.NAMESPACE)
public class BodyHeight extends BaseDataPoint {

    @JsonProperty(value = "length-unit-value", required = true)
    private LengthUnitValue lengthUnitValue;

    @JsonProperty(value = "effective-time-frame", required = true)
    private TimeFrame effectiveTimeFrame;

    public static final String SCHEMA_BODY_HEIGHT = "body-height";

    public BodyHeight() {
    }

    public BodyHeight(LengthUnitValue lengthUnitValue, TimeFrame effectiveTimeFrame) {
        this.lengthUnitValue = lengthUnitValue;
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_BODY_HEIGHT;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getStartTime();
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
