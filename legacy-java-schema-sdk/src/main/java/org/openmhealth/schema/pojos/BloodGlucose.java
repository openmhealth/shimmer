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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;
import org.openmhealth.schema.pojos.serialize.LabeledEnumSerializer;
import org.openmhealth.schema.pojos.serialize.PositionDuringMeasurementDeserializer;

import java.math.BigDecimal;


/**
 * @author Danilo Bonilla
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = BloodGlucose.SCHEMA_BLOOD_GLUCOSE, namespace = DataPoint.NAMESPACE)
public class BloodGlucose extends BaseDataPoint {

    @JsonProperty(value = "blood_glucose", required = true)
    private BloodGlucoseUnitValue bloodGlucose;

    @JsonProperty(value = "descriptive_statistic", required = false)
    private DescriptiveStatistic descriptiveStatistic;

    @JsonProperty(value = "user_notes", required = false)
    private String notes;

    @JsonProperty(value = "temporal_relationship_to_meal", required = false)
    private TemporalRelationshipToMeal temporalRelationshipToMeal;

    @JsonProperty(value = "temporal_relationship_to_sleep", required = false)
    private TemporalRelationshipToSleep temporalRelationshipToSleep;

    @JsonProperty(value = "blood_specimen_type", required = false)
    @JsonSerialize(using = LabeledEnumSerializer.class)
    @JsonDeserialize(using = PositionDuringMeasurementDeserializer.class)
    private BloodSpecimenType bloodSpecimenType;

    public static final String SCHEMA_BLOOD_GLUCOSE = "blood_glucose";

    public BloodGlucose() {
    }

    public BloodGlucose(BigDecimal value, BloodGlucoseUnitValue.Unit unit) {
        this.bloodGlucose = new BloodGlucoseUnitValue(value, unit);
    }

    @Override
    @JsonIgnore
    public String getSchemaName() {
        return SCHEMA_BLOOD_GLUCOSE;
    }

    public DescriptiveStatistic getDescriptiveStatistic() {
        return descriptiveStatistic;
    }

    public void setDescriptiveStatistic(DescriptiveStatistic descriptiveStatistic) {
        this.descriptiveStatistic = descriptiveStatistic;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public TemporalRelationshipToMeal getTemporalRelationshipToMeal() {
        return temporalRelationshipToMeal;
    }

    public void setTemporalRelationshipToMeal(TemporalRelationshipToMeal temporalRelationshipToMeal) {
        this.temporalRelationshipToMeal = temporalRelationshipToMeal;
    }

    public TemporalRelationshipToSleep getTemporalRelationshipToSleep() {
        return temporalRelationshipToSleep;
    }

    public void setTemporalRelationshipToSleep(TemporalRelationshipToSleep temporalRelationshipToSleep) {
        this.temporalRelationshipToSleep = temporalRelationshipToSleep;
    }

    public BloodSpecimenType getBloodSpecimenType() {
        return bloodSpecimenType;
    }

    public void setBloodSpecimenType(BloodSpecimenType bloodSpecimenType) {
        this.bloodSpecimenType = bloodSpecimenType;
    }

    public BloodGlucoseUnitValue getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(BloodGlucoseUnitValue bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }
}
