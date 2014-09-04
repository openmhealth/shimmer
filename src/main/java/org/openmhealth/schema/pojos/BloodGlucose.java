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
import org.openmhealth.schema.pojos.serialize.LabeledEnumSerializer;
import org.openmhealth.schema.pojos.serialize.PositionDuringMeasurementDeserializer;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = BloodGlucose.SCHEMA_BLOOD_GLUCOSE, namespace = DataPoint.NAMESPACE)
public class BloodGlucose extends BaseDataPoint {

    @JsonProperty(value = "blood-glucose", required = true)
    private BloodGlucoseUnitValue bloodGlucose;

    @JsonProperty(value = "effective-time-frame", required = false)
    private TimeFrame effectiveTimeFrame;

    @JsonProperty(value = "descriptive-statistic", required = false)
    private DescriptiveStatistic descriptiveStatistic;

    @JsonProperty(value = "user-notes", required = false)
    private String notes;

    @JsonProperty(value = "temporal-relationship-to-meal", required = false)
    private TemporalRelationshipToMeal temporalRelationshipToMeal;

    @JsonProperty(value = "temporal-relationship-to-sleep", required = false)
    private TemporalRelationshipToSleep temporalRelationshipToSleep;

    @JsonProperty(value = "blood-specimen-type", required = false)
    @JsonSerialize(using = LabeledEnumSerializer.class)
    @JsonDeserialize(using = PositionDuringMeasurementDeserializer.class)
    private BloodSpecimenType bloodSpecimenType;

    public static final String SCHEMA_BLOOD_GLUCOSE = "blood-glucose";

    public BloodGlucose() {
    }

    public BloodGlucose(BigDecimal value, BloodGlucoseUnitValue.Unit unit) {
        this.bloodGlucose = new BloodGlucoseUnitValue(value, unit);
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_BLOOD_GLUCOSE;
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

    public DescriptiveStatistic getDescriptiveStatistic() {
        return descriptiveStatistic;
    }

    public void setDescriptiveStatistic(DescriptiveStatistic descriptiveStatistic) {
        this.descriptiveStatistic = descriptiveStatistic;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public TemporalRelationshipToMeal getTemporalRelationshipToMeal() {
        return temporalRelationshipToMeal;
    }

    public void setTemporalRelationshipToMeal(TemporalRelationshipToMeal temporalRelationshipToMeal) {
        this.temporalRelationshipToMeal = temporalRelationshipToMeal;
    }

    public TemporalRelationshipToSleep getTemporalRelationshipToSleep() {
        return temporalRelationshipToSleep;
    }

    public void setTemporalRelationshipToSleep(TemporalRelationshipToSleep temporalRelationshipToSleep) {
        this.temporalRelationshipToSleep = temporalRelationshipToSleep;
    }

    public BloodSpecimenType getBloodSpecimenType() {
        return bloodSpecimenType;
    }

    public void setBloodSpecimenType(BloodSpecimenType bloodSpecimenType) {
        this.bloodSpecimenType = bloodSpecimenType;
    }

    public BloodGlucoseUnitValue getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(BloodGlucoseUnitValue bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }
}
