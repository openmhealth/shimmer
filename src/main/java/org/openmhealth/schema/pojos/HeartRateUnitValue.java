package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openmhealth.schema.pojos.serialize.dates.HeartRateUnitDeserializer;
import org.openmhealth.schema.pojos.serialize.dates.LabeledEnumSerializer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeartRateUnitValue {

    @JsonProperty(value = "value", required = true)
    private Integer value;

    @JsonProperty(value = "unit", required = true)
    @JsonSerialize(using = LabeledEnumSerializer.class)
    @JsonDeserialize(using = HeartRateUnitDeserializer.class)
    private Unit unit;

    public enum Unit implements LabeledEnum {

        bpm("beats/min");

        private String label;

        Unit(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public static Unit valueForLabel(String label) {
            for (Unit unit : Unit.values()) {
                if (unit.getLabel().equals(label)) {
                    return unit;
                }
            }
            return null;
        }
    }

    public HeartRateUnitValue(Integer value, Unit unit) {
        this.value = value;
        this.unit = unit;
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
}
