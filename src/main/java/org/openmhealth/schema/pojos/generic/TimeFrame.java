package org.openmhealth.schema.pojos.generic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.serialize.JodaTimeDateSerializer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeFrame {

    @JsonProperty(value = "start-time", required = false)
    @JsonSerialize(using = JodaTimeDateSerializer.class)
    public DateTime startTime;

    @JsonProperty(value = "end-time", required = false)
    @JsonSerialize(using = JodaTimeDateSerializer.class)
    private DateTime endTime;

    @JsonProperty(value = "duration", required = false)
    private DurationUnitValue duration;

    @JsonProperty(value = "part-of-day", required = false)
    private PartOfDay partOfDay;

    public enum PartOfDay {morning, noon, afternoon, evening}

    public TimeFrame() {
    }

    public PartOfDay getPartOfDay() {
        return partOfDay;
    }

    public void setPartOfDay(PartOfDay partOfDay) {
        this.partOfDay = partOfDay;
    }

    public DurationUnitValue getDuration() {
        return duration;
    }

    public void setDuration(DurationUnitValue duration) {
        this.duration = duration;
    }

    public TimeFrame(DateTime startTime, DateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }
}
