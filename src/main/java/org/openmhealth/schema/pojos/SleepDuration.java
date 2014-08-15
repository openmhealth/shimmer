package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = SleepDuration.SCHEMA_SLEEP_DURATION, namespace = DataPoint.NAMESPACE)
public class SleepDuration implements DataPoint {

    @JsonProperty(value = "effective-time")
    private TimeFrame effectiveTime;

    public static final String SCHEMA_SLEEP_DURATION = "sleep-duration";

    public SleepDuration() {
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_SLEEP_DURATION;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTime.getStartTime();
    }

    public TimeFrame getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(TimeFrame effectiveTime) {
        this.effectiveTime = effectiveTime;
    }
}
