package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystolicBloodPressure {

    @JsonProperty(value = "value", required = true)
    private BigDecimal value;

    @JsonProperty(value = "unit", required = true)
    private BloodPressureUnit unit;

    public SystolicBloodPressure() {
    }

    public SystolicBloodPressure(BigDecimal value, BloodPressureUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public enum Unit {mmHg}

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BloodPressureUnit getUnit() {
        return unit;
    }

    public void setUnit(BloodPressureUnit unit) {
        this.unit = unit;
    }
}
