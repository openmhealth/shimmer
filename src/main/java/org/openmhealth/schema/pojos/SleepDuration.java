package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.TimeInterval;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = SleepDuration.SCHEMA_SLEEP_DURATION, namespace = DataPoint.NAMESPACE)
public class SleepDuration extends BaseDataPoint {

    @JsonProperty(value = "effective_time_frame")
    private TimeInterval effectiveTimeFrame;

    @JsonProperty(value = "sleep_duration")
    private SleepDurationUnitValue sleepDurationUnitValue;

    @JsonProperty(value = "user_notes", required = false)
    private String notes;

    public static final String SCHEMA_SLEEP_DURATION = "sleep_duration";

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
        return effectiveTimeFrame.getDateTime();
    }

    public TimeInterval getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeInterval effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public SleepDurationUnitValue getSleepDurationUnitValue() {
        return sleepDurationUnitValue;
    }

    public void setSleepDurationUnitValue(SleepDurationUnitValue sleepDurationUnitValue) {
        this.sleepDurationUnitValue = sleepDurationUnitValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
