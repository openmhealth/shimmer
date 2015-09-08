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

package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.TemporalRelationshipToPhysicalActivity.AT_REST;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/heartrate">API documentation</a>
 */
public class JawboneHeartRateDataPointMapper extends JawboneDataPointMapper<HeartRate> {

    @Override
    protected Optional<HeartRate> getMeasure(JsonNode listEntryNode) {

        Long restingHeartRate = asRequiredLong(listEntryNode, "resting_heartrate");

        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(restingHeartRate)
                .setTemporalRelationshipToPhysicalActivity(AT_REST);

        setEffectiveTimeFrame(heartRateBuilder, listEntryNode);

        return Optional.of(heartRateBuilder.build());
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode) {
        // TODO add reference
        return true; // Jawbone explicitly states that heart rate data only comes from their sensor-based devices
    }
}
