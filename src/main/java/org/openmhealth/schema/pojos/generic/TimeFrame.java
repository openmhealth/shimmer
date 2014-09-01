package org.openmhealth.schema.pojos.generic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.serialize.dates.ISODateDeserializer;
import org.openmhealth.schema.pojos.serialize.dates.ISODateSerializer;

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
        if (timeInterval.getDate() != null) {
            return timeInterval.getDate();
        }
        if (timeInterval.getStartTime() != null) {
            return timeInterval.getStartTime();
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
        return null;
    }

    public static TimeFrame withTimeInterval(DateTime startTime, DateTime endTime) {
        return null;
    }

    public static TimeFrame withTimeInterval(DateTime startTime,
                                             Double durationValue, DurationUnitValue.DurationUnit unit) {
        return null;
    }
}
