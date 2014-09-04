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

public enum TemporalRelationshipToMeal implements LabeledEnum {

    fasting("fasting"),

    not_fasting("not fasting"),

    before_meal("before meal"),

    after_meal("after meal"),

    before_breakfast("before_breakfast"),

    after_breakfast("after breakfast"),

    before_lunch("before lunch"),

    after_lunch("after lunch"),

    before_dinner("before dinner"),

    after_dinner("after dinner"),

    two_hours_postprandial("2 hours postprandial");

    private String label;

    TemporalRelationshipToMeal(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static TemporalRelationshipToMeal valueForLabel(String label) {
        for (TemporalRelationshipToMeal val : TemporalRelationshipToMeal.values()) {
            if (val.getLabel().equals(label)) {
                return val;
            }
        }
        return null;
    }
}
