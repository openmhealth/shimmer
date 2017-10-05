/*
 * Copyright 2017 Open mHealth
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
import org.openmhealth.schema.domain.omh.Speed;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.SpeedUnit.METERS_PER_SECOND;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged speed" endpoint responses (derived:com.google.speed:com.google.android.gms:merge_speed)
 * to {@link Speed} objects.
 *
 * @author Emerson Farrugia
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitSpeedDataPointMapper extends GoogleFitDataPointMapper<Speed> {

    @Override
    protected Optional<DataPoint<Speed>> asDataPoint(JsonNode listNode) {

        JsonNode listValueNode = asRequiredNode(listNode, "value");
        double speedValue = asRequiredDouble(listValueNode.get(0), "fpVal");

        Speed speed = new Speed.Builder(METERS_PER_SECOND.newUnitValue(speedValue), getTimeFrame(listNode)).build();

        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        return Optional.of(newDataPoint(speed, originDataSourceId.orElse(null)));
    }
}
