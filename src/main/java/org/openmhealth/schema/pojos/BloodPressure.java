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
import org.openmhealth.schema.pojos.serialize.TemporalRelationshipToPhysicalActivityDeserializer;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = BloodPressure.SCHEMA_BLOOD_PRESSURE, namespace = DataPoint.NAMESPACE)
public class BloodPressure extends BaseDataPoint {

    @JsonProperty(value = "systolic-blood-pressure", required = false)
    private SystolicBloodPressure systolic;

    @JsonProperty(value = "diastolic-blood-pressure", required = false)
    private DiastolicBloodPressure diastolic;

    @JsonProperty(value = "effective-time-frame", required = false)
    private TimeFrame effectiveTimeFrame;

    @JsonProperty(value = "position-during-measurement", required = false)
    @JsonSerialize(using = LabeledEnumSerializer.class)
    @JsonDeserialize(using = TemporalRelationshipToPhysicalActivityDeserializer.class)
    private PositionDuringMeasurement positionDuringMeasurement;

    @JsonProperty(value = "descriptive-statistic", required = false)
    private DescriptiveStatistic descriptiveStatistic;

    public static final String SCHEMA_BLOOD_PRESSURE = "blood-pressure";

    @JsonProperty(value = "notes", required = false)
    private String notes;

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_BLOOD_PRESSURE;
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

    public PositionDuringMeasurement getPositionDuringMeasurement() {
        return positionDuringMeasurement;
    }

    public void setPositionDuringMeasurement(PositionDuringMeasurement positionDuringMeasurement) {
        this.positionDuringMeasurement = positionDuringMeasurement;
    }

    public SystolicBloodPressure getSystolic() {
        return systolic;
    }

    public void setSystolic(SystolicBloodPressure systolic) {
        this.systolic = systolic;
    }

    public DiastolicBloodPressure getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(DiastolicBloodPressure diastolic) {
        this.diastolic = diastolic;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public DescriptiveStatistic getDescriptiveStatistic() {
        return descriptiveStatistic;
    }

    public void setDescriptiveStatistic(DescriptiveStatistic descriptiveStatistic) {
        this.descriptiveStatistic = descriptiveStatistic;
    }
}
