/*
 * Copyright 2016 Open mHealth
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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.OxygenSaturation;
import org.openmhealth.schema.domain.omh.TypedUnitValue;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.openmhealth.schema.domain.omh.OxygenSaturation.MeasurementMethod.PULSE_OXIMETRY;
import static org.openmhealth.schema.domain.omh.OxygenSaturation.MeasurementSystem.PERIPHERAL_CAPILLARY;
import static org.openmhealth.schema.domain.omh.PercentUnit.PERCENT;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredBigDecimal;


/**
 * A mapper that translates responses from the iHealth <code>/spo2.json</code> endpoint into {@link OxygenSaturation}
 * measures.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://developer.ihealthlabs.com/dev_documentation_RequestfordataofBloodOxygen.htm">endpoint
 * documentation</a>
 */
public class IHealthOxygenSaturationDataPointMapper extends IHealthDataPointMapper<OxygenSaturation> {

    @Override
    protected String getListNodeName() {
        return "BODataList";
    }

    @Override
    protected Optional<String> getMeasureUnitNodeName() {

        return Optional.empty();
    }

    @Override
    protected Optional<DataPoint<OxygenSaturation>> asDataPoint(JsonNode listEntryNode,
            Integer measureUnitMagicNumber) {

        BigDecimal bloodOxygenValue = asRequiredBigDecimal(listEntryNode, "BO");

        // iHealth has stated that missing values would most likely be represented as a 0 value for the field
        if (bloodOxygenValue.compareTo(ZERO) == 0) {
            return Optional.empty();
        }

        OxygenSaturation.Builder oxygenSaturationBuilder =
                new OxygenSaturation.Builder(new TypedUnitValue<>(PERCENT, bloodOxygenValue))
                        .setMeasurementMethod(PULSE_OXIMETRY)
                        .setMeasurementSystem(PERIPHERAL_CAPILLARY);

        getEffectiveTimeFrameAsDateTime(listEntryNode).ifPresent(oxygenSaturationBuilder::setEffectiveTimeFrame);
        getUserNoteIfExists(listEntryNode).ifPresent(oxygenSaturationBuilder::setUserNotes);

        OxygenSaturation oxygenSaturation = oxygenSaturationBuilder.build();

        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, oxygenSaturation), oxygenSaturation));
    }
}
