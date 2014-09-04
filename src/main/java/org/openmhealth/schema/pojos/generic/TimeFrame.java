package org.openmhealth.schema.pojos.generic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.serialize.dates.ISODateDeserializer;
import org.openmhealth.schema.pojos.serialize.dates.ISODateSerializer;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeFrame {

    @JsonProperty(value = "date-time", required = false)
    @JsonSerialize(using = ISODateSerializer.class)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private DateTime dateTime;

    @JsonProperty(value = "time-interval", required = false)
    private TimeInterval timeInterval;

    public TimeFrame() {
    }

    @JsonIgnore
    public DateTime getTimestamp() {
        if (dateTime != null) {
            return dateTime;
        }
        if (timeInterval != null && timeInterval.getDate() != null) {
            return timeInterval.getDate();
        }
        if (timeInterval != null && timeInterval.getDateTime() != null) {
            return timeInterval.getDateTime();
        }
        return null;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public static TimeFrame withDateTime(DateTime dateTime) {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDateTime(dateTime);
        return timeFrame;
    }

    public static TimeFrame withTimeInterval(DateTime startTime, DateTime endTime) {
        TimeFrame timeFrame = new TimeFrame();
        TimeInterval interval = new TimeInterval();
        interval.setDateTime(startTime);
        interval.setEndTime(endTime);
        timeFrame.setTimeInterval(interval);
        return timeFrame;
    }

    public static TimeFrame withTimeInterval(DateTime startTime,
                                             Double durationValue,
                                             DurationUnitValue.DurationUnit unit) {
        TimeFrame timeFrame = new TimeFrame();
        TimeInterval interval = new TimeInterval();
        interval.setDateTime(startTime);
        DurationUnitValue durationUnitValue = new DurationUnitValue();
        durationUnitValue.setUnit(unit);
        durationUnitValue.setValue(new BigDecimal(durationValue));
        interval.setDuration(durationUnitValue);
        timeFrame.setTimeInterval(interval);
        return timeFrame;
    }
}
