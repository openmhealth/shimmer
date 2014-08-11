package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class DiastolicBloodPressure {

    @JsonProperty(value = "value", required = true)
    private BigDecimal value;

    @JsonProperty(value = "unit", required = true)
    private BloodPressureUnit unit;

    public DiastolicBloodPressure() {
    }

    public DiastolicBloodPressure(BigDecimal value, BloodPressureUnit unit) {
        this.value = value;
        this.unit = unit;
    }

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
