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

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.BloodPressureUnit.MM_OF_MERCURY;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * A mapper that translates responses from the iHealth <code>/bp.json</code> endpoint into {@link BloodPressure}
 * measures.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBloodPressure.htm">endpoint
 * documentation</a>
 */
public class IHealthBloodPressureDataPointMapper extends IHealthDataPointMapper<BloodPressure> {

    // Reference for conversion: http://www.ncbi.nlm.nih.gov/pmc/articles/PMC1603212/
    static final double KPA_TO_MMHG_CONVERSION_RATE = 7.50;

    static final int MMHG_UNIT_MAGIC_NUMBER = 0;
    static final int KPA_UNIT_MAGIC_NUMBER = 1;

    @Override
    protected String getListNodeName() {
        return "BPDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.of("BPUnit");
    }

    @Override
    protected Optional<DataPoint<BloodPressure>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        checkNotNull(measureUnitMagicNumber);

        double systolicValue =
                getBloodPressureValueInMmHg(asRequiredDouble(listEntryNode, "HP"), measureUnitMagicNumber);
        SystolicBloodPressure systolicBloodPressure = new SystolicBloodPressure(MM_OF_MERCURY, systolicValue);

        double diastolicValue =
                getBloodPressureValueInMmHg(asRequiredDouble(listEntryNode, "LP"), measureUnitMagicNumber);
        DiastolicBloodPressure diastolicBloodPressure = new DiastolicBloodPressure(MM_OF_MERCURY, diastolicValue);

        BloodPressure.Builder bloodPressureBuilder =
                new BloodPressure.Builder(systolicBloodPressure, diastolicBloodPressure);

        getEffectiveTimeFrameAsDateTime(listEntryNode).ifPresent(bloodPressureBuilder::setEffectiveTimeFrame);

        getUserNoteIfExists(listEntryNode).ifPresent(bloodPressureBuilder::setUserNotes);

        BloodPressure bloodPressure = bloodPressureBuilder.build();
        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, bloodPressure), bloodPressure));
    }

    /**
     * @param measureUnitMagicNumber The number from the iHealth response representing the unit of measure.
     * @return The corresponding OMH schema unit of measure for blood pressure.
     */
    protected double getBloodPressureValueInMmHg(double rawBpValue, Integer measureUnitMagicNumber) {

        if (measureUnitMagicNumber.equals(MMHG_UNIT_MAGIC_NUMBER)) {
            return rawBpValue;
        }
        else if (measureUnitMagicNumber.equals(KPA_UNIT_MAGIC_NUMBER)) {
            return rawBpValue * KPA_TO_MMHG_CONVERSION_RATE;
        }
        else {
            throw new UnsupportedOperationException();
        }
    }
}
