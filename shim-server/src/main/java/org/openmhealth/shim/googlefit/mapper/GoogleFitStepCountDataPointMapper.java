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

package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount2;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged step count delta" endpoint responses (derived:com.google.step_count.delta:com.google.android.gms:merge_step_deltas)
 * to {@link StepCount2} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitStepCountDataPointMapper extends GoogleFitDataPointMapper<StepCount2> {

    @Override
    protected Optional<DataPoint<StepCount2>> asDataPoint(JsonNode listNode) {

        JsonNode listValueNode = asRequiredNode(listNode, "value");
        long stepCountValue = asRequiredLong(listValueNode.get(0), "intVal");

        if (stepCountValue == 0) {
            return Optional.empty();
        }

        StepCount2 stepCount = new StepCount2.Builder(stepCountValue, getTimeFrame(listNode))
                .build();

        Optional<String> originSourceId = asOptionalString(listNode, "originDataSourceId");

        return Optional.of(newDataPoint(stepCount, originSourceId.orElse(null)));
    }
}
