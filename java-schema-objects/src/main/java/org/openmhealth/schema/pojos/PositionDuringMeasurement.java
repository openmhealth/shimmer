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

/**
 * Describes the position the patient was in
 * when a clinical measurement was taken.
 *
 * @author Danilo Bonilla
 */
public enum PositionDuringMeasurement implements LabeledEnum {

    sitting("sitting"), lying_down("lying down"), standing("standing");

    private String label;

    PositionDuringMeasurement(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static PositionDuringMeasurement valueForLabel(String label) {
        for (PositionDuringMeasurement value : PositionDuringMeasurement.values()) {
            if (value.getLabel().equals(label)) {
                return value;
            }
        }
        return null;
    }

}
