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
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;

import java.util.Optional;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.WeightSetFeed+json responses to {@link
 * BodyWeight} objects.
 *
 * @author Emerson Farrugia
 * @see <a href="http://runkeeper.com/developer/healthgraph/weight-sets#past">API documentation</a>
 */
public class RunkeeperBodyWeightDataPointMapper extends RunkeeperDataPointMapper<BodyWeight> {

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode itemNode) {

        throw new UnsupportedOperationException("This measure cannot be mapped without time zone information.");
    }
}
