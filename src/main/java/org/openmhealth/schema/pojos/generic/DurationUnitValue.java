package org.openmhealth.schema.pojos.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class DurationUnitValue {

    @JsonProperty(value = "value", required = true)
    private BigDecimal value;

    @JsonProperty(value = "unit", required = true)
    private DurationUnit unit;

    public enum DurationUnit {ps, ns, mics, ms, sec, min, h, d, wk, mo, yr}

    public DurationUnitValue() {
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public DurationUnit getUnit() {
        return unit;
    }

    public void setUnit(DurationUnit unit) {
        this.unit = unit;
    }
}
