package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = NumberOfSteps.SCHEMA_NUMBER_OF_STEPS, namespace = DataPoint.NAMESPACE)
public class NumberOfSteps implements DataPoint {

    @JsonProperty(value = "value", required = true)
    private Integer value;

    @JsonProperty(value = "effective-time-frame", required = true)
    private TimeFrame effectiveTimeFrame;

    public static final String SCHEMA_NUMBER_OF_STEPS = "number-of-steps";

    public NumberOfSteps() {
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_NUMBER_OF_STEPS;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getStartTime();
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
