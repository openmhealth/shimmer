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
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;


/**
 * @author Danilo Bonilla
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = Activity.SCHEMA_ACTIVITY, namespace = DataPoint.NAMESPACE)
public class Activity extends BaseDataPoint {

    @JsonProperty(value = "activity_name", required = true)
    private String activityName;

    @JsonProperty(value = "distance", required = false)
    private LengthUnitValue distance;

    @JsonProperty(value = "reported_activity_intensity", required = false)
    private ActivityIntensity reportedActivityIntensity;

    public enum ActivityIntensity {light, moderate, vigorous}

    @JsonProperty("effective_time_frame")
    private TimeFrame effectiveTimeFrame;

    public static final String SCHEMA_ACTIVITY = "physical_activity";

    public Activity() {
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_ACTIVITY;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getTimestamp();
    }

    public LengthUnitValue getDistance() {
        return distance;
    }

    public void setDistance(LengthUnitValue distance) {
        this.distance = distance;
    }

    public ActivityIntensity getReportedActivityIntensity() {
        return reportedActivityIntensity;
    }

    public void setReportedActivityIntensity(ActivityIntensity reportedActivityIntensity) {
        this.reportedActivityIntensity = reportedActivityIntensity;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }
}
