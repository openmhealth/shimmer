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

package org.openmhealth.shim.microsoft.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * A mapper from Microsoft Resource API  <version>/me/Summaries/ focusing solely on stepsTaken
 *
 * @author Emerson Farrugia
 * @author Eric Jain
 * @see <a href="https://build.misfit.com/docs/references#APIReferences-Summary">API documentation</a>
 */
public class MicrosoftStepCountDataPointMapper extends MicrosoftDataPointMapper<StepCount> {

    @Override
    protected String getListNodeName() {
        return "summaries";
    }

    @Override
    public Optional<DataPoint<StepCount>> asDataPoint(JsonNode summaryNode) {

        checkNotNull(summaryNode);

        Long stepCount = asRequiredLong(summaryNode, "stepsTaken");

        if (stepCount == 0) {
            return Optional.empty();
        }

        StepCount builder = new StepCount.Builder(stepCount)
                .setEffectiveTimeFrame(getStartTime(summaryNode))
                .build();


        return Optional.of(newDataPoint(builder, RESOURCE_API_SOURCE_NAME, null, null));
    }

}
