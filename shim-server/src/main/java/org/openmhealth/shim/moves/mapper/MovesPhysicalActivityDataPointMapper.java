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
import org.openmhealth.schema.domain.omh.KcalUnitValue;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.schema.domain.omh.PhysicalActivity;
import org.openmhealth.schema.domain.omh.TimeFrame;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredString;


/**
 * A mapper that translates responses from the Moves Resource API <code>/user/storyline/daily</code> endpoint into
 * {@link PhysicalActivity} data points.
 *
 * @author Emerson Farrugia
 * @author Jared Sieling
 * @see <a href="https://dev.moves-app.com/docs/api_storyline">API documentation</a>
 */
public class MovesPhysicalActivityDataPointMapper extends MovesActivityNodeDataPointMapper<PhysicalActivity> {

    @Override
    protected Optional<PhysicalActivity> newMeasure(JsonNode node) {

        Optional<TimeFrame> timeFrame = getTimeFrame(node);

        if (!timeFrame.isPresent()) {
            return empty();
        }

        String activityName = asRequiredString(node, "activity");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);
        builder.setEffectiveTimeFrame(timeFrame.get());

        asOptionalDouble(node, "distance")
                .ifPresent(distanceInM -> builder.setDistance(new LengthUnitValue(METER, distanceInM)));

        asOptionalDouble(node, "calories")
                .ifPresent(calories -> builder.setCaloriesBurned(new KcalUnitValue(KILOCALORIE, calories)));

        return Optional.of(builder.build());
    }
}
