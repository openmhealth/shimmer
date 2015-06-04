/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.schema.pojos.generic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.math.BigDecimal;


/**
 * @author Danilo Bonilla
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @Override
    public boolean equals(Object that) {
    	return that instanceof DurationUnitValue
    		&& equals((DurationUnitValue) that);
    }

    private boolean equals(DurationUnitValue that) {
    	return Objects.equal(value, that.value)
    		&& Objects.equal(unit, that.unit);
    }

    @Override
    public int hashCode() {
    	return Objects.hashCode(value, unit);
    }

    @Override
    public String toString() {
    	return value + " " + unit;
    }
}
