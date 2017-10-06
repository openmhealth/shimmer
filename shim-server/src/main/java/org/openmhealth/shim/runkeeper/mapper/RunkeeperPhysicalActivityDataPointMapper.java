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

package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.schema.domain.omh.PhysicalActivity;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredString;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.FitnessActivityFeed+json responses to {@link
 * PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Danilo Bonilla
 * @see <a href="http://runkeeper.com/developer/healthgraph/fitness-activities#past">API documentation</a>
 */
public class RunkeeperPhysicalActivityDataPointMapper extends RunkeeperDataPointMapper<PhysicalActivity> {

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode itemNode) {

        PhysicalActivity measure = getMeasure(itemNode);
        DataPointHeader header = getDataPointHeader(itemNode, measure);

        return Optional.of(new DataPoint<>(header, measure));
    }

    private PhysicalActivity getMeasure(JsonNode itemNode) {

        String activityName = asRequiredString(itemNode, "type");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        getOptionalTimeFrame(itemNode).ifPresent(builder::setEffectiveTimeFrame);

        asOptionalDouble(itemNode, "total_distance")
                .ifPresent(distanceInM -> builder.setDistance(new LengthUnitValue(METER, distanceInM)));

        return builder.build();
    }
}
