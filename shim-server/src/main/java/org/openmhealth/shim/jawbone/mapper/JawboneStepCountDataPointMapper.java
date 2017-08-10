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
import org.openmhealth.schema.domain.omh.StepCount1;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 */
public class JawboneStepCountDataPointMapper extends JawboneDataPointMapper<StepCount1> {

    @Override
    protected Optional<StepCount1> getMeasure(JsonNode listEntryNode) {

        long stepCountValue = asRequiredLong(listEntryNode, "details.steps");

        if (stepCountValue <= 0) {
            return Optional.empty();
        }

        StepCount1.Builder stepCountBuilder = new StepCount1.Builder(stepCountValue);

        setEffectiveTimeFrame(stepCountBuilder, listEntryNode);

        return Optional.of(stepCountBuilder.build());
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode) {

        // the moves endpoint, from which step count is derived, only contains data sensed by Jawbone
        // devices or by the Jawbone UP app
        return true;
    }
}
