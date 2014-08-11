package org.openmhealth.schema.pojos;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Activity {

    @JsonProperty(value = "activity-name", required = true)
    private String activityName;

    @JsonProperty(value = "distance", required = false)
    private LengthUnitValue distance;

    @JsonProperty(value = "reported-activity-intensity", required = false)
    private ActivityIntensity reportedActivityIntensity;

    public enum ActivityIntensity {light, moderate, vigorous}

    @JsonProperty("effective-time-frame")
    private TimeFrame effectiveTimeFrame;

    public Activity() {
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
