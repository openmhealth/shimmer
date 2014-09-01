package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;
import org.openmhealth.schema.pojos.generic.TimeFrame;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = HeartRate.SCHEMA_HEART_RATE, namespace = DataPoint.NAMESPACE)
public class HeartRate extends BaseDataPoint {

    @JsonProperty(value = "value", required = true)
    private Integer value;

    @JsonProperty(value = "unit", required = true)
    private Unit unit;

    @JsonProperty(value = "effective-time-frame", required = false)
    private TimeFrame effectiveTimeFrame;

    @JsonProperty(value = "numeric-descriptor", required = false)
    private DescriptiveStatistic descriptiveStatistic;

    @JsonProperty(value = "notes", required = false)
    private String notes;

    public static final String SCHEMA_HEART_RATE = "heart-rate";

    public HeartRate() {
    }

    public HeartRate(Integer value, Unit unit) {
        this.value = value;
        this.unit = unit;
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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public enum Unit {bpm}

}
