/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.ihealth.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.openmhealth.schema.domain.omh.*;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBloodGlucoseDataPointMapper extends IHealthDataPointMapper<BloodGlucose> {

    protected static ImmutableMap<String, TemporalRelationshipToMeal> iHealthBloodGlucoseRelationshipToMeal;

    @Override
    protected List<String> getListNodeNames() {
        return singletonList("BGDataList");
    }

    @Override
    protected Optional<String> getUnitPropertyNameForMeasure() {
        return Optional.of("BGUnit");
    }

    public IHealthBloodGlucoseDataPointMapper() {

        initializeTemporalRelationshipToFoodMap();
    }

    @Override
    protected Optional<DataPoint<BloodGlucose>> asDataPoint(JsonNode listNode, Integer measureUnit) {

        double bloodGlucoseValue = asRequiredDouble(listNode, "BG");
        if (bloodGlucoseValue == 0) {

            return Optional.empty();
        }

        BloodGlucoseUnit bloodGlucoseUnit =
                IHealthBloodGlucoseUnit.fromIHealthMagicNumber(measureUnit).getBloodGlucoseUnit();

        BloodGlucose.Builder bloodGlucoseBuilder =
                new BloodGlucose.Builder(new TypedUnitValue<>(bloodGlucoseUnit, bloodGlucoseValue));

        Optional<String> dinnerSituation = asOptionalString(listNode, "DinnerSituation");

        if (dinnerSituation.isPresent()) {

            TemporalRelationshipToMeal temporalRelationshipToMeal =
                    iHealthBloodGlucoseRelationshipToMeal.get(dinnerSituation.get());

            if (temporalRelationshipToMeal != null) {
                bloodGlucoseBuilder.setTemporalRelationshipToMeal(temporalRelationshipToMeal);
            }
        }

        setEffectiveTimeFrameIfExists(listNode, bloodGlucoseBuilder);
        setUserNoteIfExists(listNode, bloodGlucoseBuilder);

        BloodGlucose bloodGlucose = bloodGlucoseBuilder.build();

        /*  The "temporal_relationship_to_medication" property is not part of the Blood Glucose schema, so its name and
            values may change or we may remove support for this property at any time. */
        asOptionalString(listNode, "DrugSituation").ifPresent(
                drugSituation -> bloodGlucose
                        .setAdditionalProperty("temporal_relationship_to_medication", drugSituation));

        return Optional.of(new DataPoint<>(createDataPointHeader(listNode, bloodGlucose), bloodGlucose));
    }

    private void initializeTemporalRelationshipToFoodMap() {

        ImmutableMap.Builder<String, TemporalRelationshipToMeal> relationshipToMealMapBuilder = ImmutableMap.builder();

        relationshipToMealMapBuilder.put("Before_breakfast", TemporalRelationshipToMeal.BEFORE_BREAKFAST)
                .put("After_breakfast", TemporalRelationshipToMeal.AFTER_BREAKFAST)
                .put("Before_lunch", TemporalRelationshipToMeal.BEFORE_LUNCH)
                .put("After_lunch", TemporalRelationshipToMeal.AFTER_LUNCH)
                .put("Before_dinner", TemporalRelationshipToMeal.BEFORE_DINNER)
                .put("After_dinner", TemporalRelationshipToMeal.AFTER_DINNER)
                .put("At_midnight", TemporalRelationshipToMeal.AFTER_DINNER);

        iHealthBloodGlucoseRelationshipToMeal = relationshipToMealMapBuilder.build();

    }

    protected enum IHealthBloodGlucoseUnit {

        mgPerDl(0, BloodGlucoseUnit.MILLIGRAMS_PER_DECILITER),
        mmolPerL(1, BloodGlucoseUnit.MILLIMOLES_PER_LITER);

        private int magicNumber;
        private BloodGlucoseUnit bgUnit;

        IHealthBloodGlucoseUnit(int magicNumber, BloodGlucoseUnit bgUnit) {

            this.magicNumber = magicNumber;
            this.bgUnit = bgUnit;
        }

        protected BloodGlucoseUnit getBloodGlucoseUnit() {
            return bgUnit;
        }

        public static IHealthBloodGlucoseUnit fromIHealthMagicNumber(int magicNumberFromResponse) {

            for (IHealthBloodGlucoseUnit type : values()) {
                if (type.magicNumber == magicNumberFromResponse) {
                    return type;
                }
            }
            throw new UnsupportedOperationException();
        }

    }


}
