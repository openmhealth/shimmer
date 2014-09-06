package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.TimeFrame;
import org.openmhealth.schema.pojos.generic.TimeInterval;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = StepCount.SCHEMA_STEP_COUNT, namespace = DataPoint.NAMESPACE)
public class StepCount extends BaseDataPoint {

    @JsonProperty(value = "step_count", required = true)
    private Integer stepCount;

    @JsonProperty(value = "effective_time_frame", required = false)
    private TimeInterval effectiveTimeFrame;

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
        return effectiveTimeFrame.getDateTime();
    }

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public TimeInterval getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeInterval effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
}
