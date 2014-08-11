package org.openmhealth.schema.pojos.generic;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MassUnitValue {

    @JsonProperty(value = "value", required = true)
    private BigDecimal value;

    @JsonProperty(value = "unit", required = true)
    private MassUnit unit;

    public enum MassUnit {fg, pg, ng, micg, mg, g, kg, metric_ton, gr, oz, lb, ton}

    public MassUnitValue() {
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public MassUnit getUnit() {
        return unit;
    }

    public void setUnit(MassUnit unit) {
        this.unit = unit;
    }
}
