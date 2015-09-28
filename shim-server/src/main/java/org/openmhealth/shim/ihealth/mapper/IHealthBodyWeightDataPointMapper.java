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
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBodyWeightDataPointMapper extends IHealthDataPointMapper<BodyWeight> {

    // Reference: https://en.wikipedia.org/wiki/Stone_(unit)
    private static final double STONE_TO_KG_FACTOR = 6.3503;

    @Override
    protected String getListNodeName() {
        return "WeightDataList";
    }

    @Override
    protected String getUnitPropertyNameForMeasure() {
        return "WeightUnit";
    }

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode listNode, int measureUnit) {

        BodyWeightUnitType bodyWeightUnitType = BodyWeightUnitType.fromIntegerValue(measureUnit);
        MassUnit bodyWeightUnit = bodyWeightUnitType.getOmhUnit();

        double bodyWeightValue = getBodyWeightValueForUnitType(listNode, bodyWeightUnitType);

        if (bodyWeightValue == 0) {

            return Optional.empty();
        }

        BodyWeight.Builder bodyWeightBuilder =
                new BodyWeight.Builder(new MassUnitValue(bodyWeightUnit, bodyWeightValue));

        setEffectiveTimeFrameIfExists(listNode,bodyWeightBuilder);
        setUserNoteIfExists(listNode,bodyWeightBuilder);

        BodyWeight bodyWeight = bodyWeightBuilder.build();

        return Optional.of(new DataPoint<>(createDataPointHeader(listNode, bodyWeight), bodyWeight));
    }

    protected double getBodyWeightValueForUnitType(JsonNode listNode,
            BodyWeightUnitType bodyWeightUnitType) {

        Double weightValueFromApi = asRequiredDouble(listNode, "WeightValue");
        return getBodyWeightValueForUnitType(weightValueFromApi, bodyWeightUnitType);
    }

    protected double getBodyWeightValueForUnitType(double bodyWeightValue, BodyWeightUnitType bodyWeightUnitType) {

        return bodyWeightValue * bodyWeightUnitType.getConversionFactorToOmh();
    }

    enum BodyWeightUnitType {

        /*
            The conversion factor handles conversions from unsupported mass units (currently the 'Stone' unit) to omh
            supported units such that:

            ValueIntoSchema = ValueFromApi * Conversion

            We map stone into kg because it is the SI unit for mass and the most widely accepted for measuring human
            body weight in a clinical/scientific context.
        */
        kg(0, MassUnit.KILOGRAM, 1),
        lb(1, MassUnit.POUND, 1),
        stone(2, MassUnit.KILOGRAM, STONE_TO_KG_FACTOR);

        private final MassUnit omhUnit;
        private final double conversionFactorToOmh;
        private int magicNumber;

        BodyWeightUnitType(int magicNumber, MassUnit omhUnit, double conversionFactor) {
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

        public static BodyWeightUnitType fromIntegerValue(int unitValueFromApi) {

            for (BodyWeightUnitType type : values()) {
                if (type.magicNumber == unitValueFromApi) {
                    return type;
                }
            }

            throw new UnsupportedOperationException();
        }

    }
}
