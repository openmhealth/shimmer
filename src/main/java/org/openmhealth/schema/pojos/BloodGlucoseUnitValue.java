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

package org.openmhealth.schema.pojos;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openmhealth.schema.pojos.serialize.BloodGlucoseUnitValueDeserializer;
import org.openmhealth.schema.pojos.serialize.LabeledEnumSerializer;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BloodGlucoseUnitValue {

    @JsonProperty(value = "value", required = true)
    private BigDecimal value;

    @JsonProperty(value = "unit", required = true)
    @JsonSerialize(using = LabeledEnumSerializer.class)
    @JsonDeserialize(using = BloodGlucoseUnitValueDeserializer.class)
    private Unit unit;

    public enum Unit implements LabeledEnum {

        mg_dL("mg/dL"),

        mmol_L("mmol/L");

        private String label;

        Unit(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        public static Unit valueForLabel(String label) {
            for (Unit val : Unit.values()) {
                if (val.getLabel().equals(label)) {
                    return val;
                }
            }
            return null;
        }
    }

    public BloodGlucoseUnitValue() {
    }

    public BloodGlucoseUnitValue(BigDecimal value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}
