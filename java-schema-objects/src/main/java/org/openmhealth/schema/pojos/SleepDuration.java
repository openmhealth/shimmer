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

package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.TimeFrame;


/**
 * @author Danilo Bonilla
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = SleepDuration.SCHEMA_SLEEP_DURATION, namespace = DataPoint.NAMESPACE)
public class SleepDuration extends BaseDataPoint {

    @JsonProperty(value = "effective_time_frame")
    private TimeFrame effectiveTimeFrame;

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
        return effectiveTimeFrame.getTimestamp();
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
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
