/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.StepCount2;
import org.openmhealth.schema.domain.omh.TimeFrame;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;


/**
 * A mapper that translates responses from the Moves Resource API <code>/user/storyline/daily</code> endpoint into
 * {@link StepCount2} data points.
 *
 * @author Emerson Farrugia
 * @see <a href="https://dev.moves-app.com/docs/api_storyline">API documentation</a>
 */
public class MovesStepCountDataPointMapper extends MovesActivityNodeDataPointMapper<StepCount2> {

    @Override
    protected Optional<StepCount2> newMeasure(JsonNode node) {

        Optional<TimeFrame> timeFrame = getTimeFrame(node);

        // a time frame seems to not be present for manually entered data, making it impossible to deduplicate
        if (!timeFrame.isPresent()) {
            return empty();
        }

        return asOptionalLong(node, "steps")
                .filter(count -> count > 0)
                .map(count -> new StepCount2.Builder(count, timeFrame.get()).build());
    }
}
