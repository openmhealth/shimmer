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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * An abstract mapper that maps iHealth responses into {@link HeartRate} measures.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class IHealthHeartRateDataPointMapper extends IHealthDataPointMapper<HeartRate> {

    @Override
    protected Optional<String> getMeasureUnitNodeName() {
        return Optional.empty();
    }

    @Override
    protected Optional<DataPoint<HeartRate>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber) {

        double heartRateValue = asRequiredDouble(listEntryNode, "HR");

        if (heartRateValue == 0) {
            return Optional.empty();
        }

        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(heartRateValue);

        getEffectiveTimeFrameAsDateTime(listEntryNode).ifPresent(heartRateBuilder::setEffectiveTimeFrame);

        getUserNoteIfExists(listEntryNode).ifPresent(heartRateBuilder::setUserNotes);

        HeartRate heartRate = heartRateBuilder.build();
        return Optional.of(new DataPoint<>(createDataPointHeader(listEntryNode, heartRate), heartRate));
    }
}
