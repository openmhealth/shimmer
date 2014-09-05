package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;
import org.openmhealth.schema.pojos.generic.TimeFrame;
import org.openmhealth.schema.pojos.serialize.HeartRateUnitDeserializer;
import org.openmhealth.schema.pojos.serialize.LabeledEnumSerializer;
import org.openmhealth.schema.pojos.serialize.TemporalRelationshipToPhysicalActivityDeserializer;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = HeartRate.SCHEMA_HEART_RATE, namespace = DataPoint.NAMESPACE)
public class HeartRate extends BaseDataPoint {

    @JsonProperty(value = "effective_time_frame", required = false)
    private TimeFrame effectiveTimeFrame;

    @JsonProperty(value = "descriptive_statistic", required = false)
    private DescriptiveStatistic descriptiveStatistic;

    @JsonProperty(value = "user_notes", required = false)
    private String userNotes;

    @JsonProperty(value = "heart_rate", required = true)
    private HeartRateUnitValue heartRate;

    @JsonProperty(value = "temporal_relationship_to_physical_activity", required = false)
    @JsonSerialize(using = LabeledEnumSerializer.class)
    @JsonDeserialize(using = TemporalRelationshipToPhysicalActivityDeserializer.class)
    private TemporalRelationshipToPhysicalActivity temporalRelationshipToPhysicalActivity;

    public static final String SCHEMA_HEART_RATE = "heart_rate";

    public HeartRate() {
    }

    public HeartRate(Integer value, HeartRateUnitValue.Unit unit) {
        this.heartRate = new HeartRateUnitValue(value, unit);
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_HEART_RATE;
    }

    @Override
    @JsonIgnore
    public DateTime getTimeStamp() {
        return effectiveTimeFrame.getTimestamp();
    }

    public DescriptiveStatistic getDescriptiveStatistic() {
        return descriptiveStatistic;
    }

    public void setDescriptiveStatistic(DescriptiveStatistic descriptiveStatistic) {
        this.descriptiveStatistic = descriptiveStatistic;
    }

    public TimeFrame getEffectiveTimeFrame() {
        return effectiveTimeFrame;
    }

    public void setEffectiveTimeFrame(TimeFrame effectiveTimeFrame) {
        this.effectiveTimeFrame = effectiveTimeFrame;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    public HeartRateUnitValue getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(HeartRateUnitValue heartRate) {
        this.heartRate = heartRate;
    }

    public TemporalRelationshipToPhysicalActivity getTemporalRelationshipToPhysicalActivity() {
        return temporalRelationshipToPhysicalActivity;
    }

    public void setTemporalRelationshipToPhysicalActivity(
        TemporalRelationshipToPhysicalActivity temporalRelationshipToPhysicalActivity) {
        this.temporalRelationshipToPhysicalActivity = temporalRelationshipToPhysicalActivity;
    }


}
