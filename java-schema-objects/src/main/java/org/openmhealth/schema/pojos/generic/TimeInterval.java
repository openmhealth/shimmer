/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.schema.pojos.generic;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.serialize.dates.ISODateDeserializer;
import org.openmhealth.schema.pojos.serialize.dates.ISODateSerializer;
import org.openmhealth.schema.pojos.serialize.dates.SimpleDateDeserializer;
import org.openmhealth.schema.pojos.serialize.dates.SimpleDateSerializer;

import java.math.BigDecimal;


/**
 * @author Danilo Bonilla
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeInterval {

    public final static String FULLDATE_FORMAT = "yyyy-MM-dd";

    @JsonProperty(value = "date", required = false)
    @JsonSerialize(using = SimpleDateSerializer.class)
    @JsonDeserialize(using = SimpleDateDeserializer.class)
    private DateTime date;

    @JsonProperty(value = "start_date_time", required = false)
    @JsonSerialize(using = ISODateSerializer.class)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private DateTime startTime;

    @JsonProperty(value = "end_date_time", required = false)
    @JsonSerialize(using = ISODateSerializer.class)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private DateTime endTime;

    @JsonProperty(value = "duration", required = false)
    private DurationUnitValue duration;

    @JsonProperty(value = "part_of_day", required = false)
    private PartOfDay partOfDay;

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
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

    public DurationUnitValue getDuration() {
        return duration;
    }

    public void setDuration(DurationUnitValue duration) {
        this.duration = duration;
    }

    public PartOfDay getPartOfDay() {
        return partOfDay;
    }

    public void setPartOfDay(PartOfDay partOfDay) {
        this.partOfDay = partOfDay;
    }

    public static TimeInterval withStartAndEnd(DateTime start, DateTime end) {
        TimeInterval interval = new TimeInterval();
        interval.setStartTime(start);
        interval.setEndTime(end);
        return interval;
    }

    public static TimeInterval withStartAndDuration(DateTime startTime,
                                                    Double durationValue,
                                                    DurationUnitValue.DurationUnit unit) {
        TimeInterval interval = new TimeInterval();
        interval.setStartTime(startTime);
        DurationUnitValue durationUnitValue = new DurationUnitValue();
        durationUnitValue.setUnit(unit);
        durationUnitValue.setValue(new BigDecimal(durationValue));
        interval.setDuration(durationUnitValue);
        return interval;
    }
}
