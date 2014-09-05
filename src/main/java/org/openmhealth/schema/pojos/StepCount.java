package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = StepCount.SCHEMA_STEP_COUNT, namespace = DataPoint.NAMESPACE)
public class StepCount extends BaseDataPoint {

    @JsonProperty(value = "value", required = true)
    private Integer value;

    @JsonProperty(value = "effective_time_frame", required = true)
    private TimeFrame effectiveTimeFrame;

    public static final String SCHEMA_STEP_COUNT = "step_count";

    public StepCount() {
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_STEP_COUNT;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getTimestamp();
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
