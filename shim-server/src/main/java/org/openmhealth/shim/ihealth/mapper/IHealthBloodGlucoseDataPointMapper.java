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
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.ihealth.domain.IHealthTemporalRelationshipToMeal;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.BloodGlucoseUnit.MILLIGRAMS_PER_DECILITER;
import static org.openmhealth.schema.domain.omh.BloodGlucoseUnit.MILLIMOLES_PER_LITER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * A mapper that translates responses from the iHealth <code>/glucose.json</code> endpoint into {@link BloodGlucose}
 * measures.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBG.htm">endpoint documentation</a>
 */
public class IHealthBloodGlucoseDataPointMapper extends IHealthDataPointMapper<BloodGlucose> {

    public static final int MG_PER_DL_MAGIC_NUMBER = 0;
    public static final int MMOL_PER_L_MAGIC_NUMBER = 1;

    @Override
    protected String getListNodeName() {
        return "BGDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.of("BGUnit");
    }


    @Override
    protected Optional<DataPoint<BloodGlucose>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        checkNotNull(measureUnitMagicNumber);

        double bloodGlucoseValue = asRequiredDouble(listEntryNode, "BG");

        if (bloodGlucoseValue == 0) {
            return Optional.empty();
        }

        BloodGlucoseUnit bloodGlucoseUnit = getBloodGlucoseUnitFromMagicNumber(measureUnitMagicNumber);

        BloodGlucose.Builder bloodGlucoseBuilder =
                new BloodGlucose.Builder(new TypedUnitValue<>(bloodGlucoseUnit, bloodGlucoseValue));

        Optional<String> relationshipToMeal = asOptionalString(listEntryNode, "DinnerSituation");

        if (relationshipToMeal.isPresent()) {

            IHealthTemporalRelationshipToMeal temporalRelationshipToMeal =
                    IHealthTemporalRelationshipToMeal.findByResponseValue(relationshipToMeal.get()).get();

            bloodGlucoseBuilder.setTemporalRelationshipToMeal(temporalRelationshipToMeal.getStandardConstant());
        }

        getEffectiveTimeFrameAsDateTime(listEntryNode).ifPresent(bloodGlucoseBuilder::setEffectiveTimeFrame);
        getUserNoteIfExists(listEntryNode).ifPresent(bloodGlucoseBuilder::setUserNotes);

        BloodGlucose bloodGlucose = bloodGlucoseBuilder.build();

        /*  The "temporal_relationship_to_medication" property is not part of the Blood Glucose schema, so its name and
            values may change or we may remove support for this property at any time. */
        asOptionalString(listEntryNode, "DrugSituation").ifPresent(
                drugSituation -> bloodGlucose
                        .setAdditionalProperty("temporal_relationship_to_medication", drugSituation));

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, bloodGlucose), bloodGlucose));
    }

    /**
     * @param measureUnitMagicNumber The number from the iHealth response representing the unit of measure.
     * @return The corresponding OMH schema unit of measure for blood glucose.
     */
    protected BloodGlucoseUnit getBloodGlucoseUnitFromMagicNumber(Integer measureUnitMagicNumber) {

        if (measureUnitMagicNumber.equals(MG_PER_DL_MAGIC_NUMBER)) {
            return MILLIGRAMS_PER_DECILITER;
        }
        else if (measureUnitMagicNumber.equals(MMOL_PER_L_MAGIC_NUMBER)) {
            return MILLIMOLES_PER_LITER;
        }
        else {
            throw new UnsupportedOperationException();
        }
    }
}
