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
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * A mapper that translates responses from the iHealth <code>/weight.json</code> endpoint into {@link BodyWeight}
 * measures.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofWeight.htm">endpoint
 * documentation</a>
 */
public class IHealthBodyWeightDataPointMapper extends IHealthDataPointMapper<BodyWeight> {

    // Reference for conversion: https://en.wikipedia.org/wiki/Stone_(unit)
    private static final double STONE_TO_KG_FACTOR = 6.3503;

    @Override
    protected String getListNodeName() {
        return "WeightDataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.of("WeightUnit");
    }

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        checkNotNull(measureUnitMagicNumber);

        IHealthBodyWeightUnit bodyWeightUnitType = IHealthBodyWeightUnit.fromIntegerValue(measureUnitMagicNumber);
        MassUnit bodyWeightUnit = bodyWeightUnitType.getOmhUnit();

        double bodyWeightValue = getBodyWeightValueForUnitType(listEntryNode, bodyWeightUnitType);

        if (bodyWeightValue == 0) {

            return Optional.empty();
        }

        BodyWeight.Builder bodyWeightBuilder =
                new BodyWeight.Builder(new MassUnitValue(bodyWeightUnit, bodyWeightValue));

        getEffectiveTimeFrameAsDateTime(listEntryNode).ifPresent(bodyWeightBuilder::setEffectiveTimeFrame);

        getUserNoteIfExists(listEntryNode).ifPresent(bodyWeightBuilder::setUserNotes);

        BodyWeight bodyWeight = bodyWeightBuilder.build();

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, bodyWeight), bodyWeight));
    }

    /**
     * @param listEntryNode A single entry from the response result array.
     * @param bodyWeightUnitType The unit type for the measure.
     * @return The body weight value for the list entry that is rendered in the correct unit.
     */
    protected double getBodyWeightValueForUnitType(JsonNode listEntryNode,
            IHealthBodyWeightUnit bodyWeightUnitType) {

        Double weightValueFromApi = asRequiredDouble(listEntryNode, "WeightValue");
        return getBodyWeightValueForUnitType(weightValueFromApi, bodyWeightUnitType);
    }

    /**
     * @param bodyWeightValue The body weight value that has been extracted from the list entry node.
     * @param bodyWeightUnitType The unit type for the measure.
     * @return A body weight value that is rendered in the correct unit.
     */
    protected double getBodyWeightValueForUnitType(double bodyWeightValue, IHealthBodyWeightUnit bodyWeightUnitType) {

        // iHealth has one unit type that is unsupported by OMH schemas, so we need to convert the value into a unit
        // system that is supported by the schemas.
        return bodyWeightValue * bodyWeightUnitType.getConversionFactorToOmh();
    }

    enum IHealthBodyWeightUnit {

        /*
            The conversion factor handles conversions from unsupported mass units (currently the 'Stone' unit) to omh
            supported units such that:

            ValueIntoSchema = ValueFromApi * Conversion

            We map STONE into KG because it is the SI unit for mass and the most widely accepted for measuring human
            body weight in a clinical/scientific context.
        */
        KG(0, MassUnit.KILOGRAM, 1),
        LB(1, MassUnit.POUND, 1),
        STONE(2, MassUnit.KILOGRAM, STONE_TO_KG_FACTOR);

        private final MassUnit omhUnit;
        private final double conversionFactorToOmh;
        private int magicNumber;

        IHealthBodyWeightUnit(int magicNumber, MassUnit omhUnit, double conversionFactor) {
            this.omhUnit = omhUnit;
            this.conversionFactorToOmh = conversionFactor;
            this.magicNumber = magicNumber;
        }

        public MassUnit getOmhUnit() {
            return omhUnit;
        }

        public double getConversionFactorToOmh() {
            return conversionFactorToOmh;
        }

        public static IHealthBodyWeightUnit fromIntegerValue(int unitValueFromApi) {

            for (IHealthBodyWeightUnit type : values()) {
                if (type.magicNumber == unitValueFromApi) {
                    return type;
                }
            }

            throw new UnsupportedOperationException();
        }

    }
}
