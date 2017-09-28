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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.SchemaSupport;
import org.openmhealth.schema.domain.omh.TimeFrame;
import org.openmhealth.shim.OptionalStreamSupport;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.OptionalStreamSupport.asStream;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalNode;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredString;


/**
 * A mapper from activity nodes in Moves Resource API <code>/user/storyline/daily</code> responses to data points.
 *
 * @author Emerson Farrugia
 * @author Jared Sieling
 * @see <a href="https://dev.moves-app.com/docs/api_storyline">API documentation</a>
 */
public abstract class MovesActivityNodeDataPointMapper<T extends SchemaSupport> extends MovesDataPointMapper<T> {

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "A single response node is allowed per call.");

        return StreamSupport.stream(responseNodes.get(0).spliterator(), false)
                .flatMap(dayNode -> asStream(asOptionalNode(dayNode, "segments")))
                .flatMap(segmentsNode -> StreamSupport.stream(segmentsNode.spliterator(), false))
                .flatMap(segmentNode -> asStream(asOptionalNode(segmentNode, "activities")))
                .flatMap(activitiesNode -> StreamSupport.stream(activitiesNode.spliterator(), false))
                .map(this::asDataPoint)
                .flatMap(OptionalStreamSupport::asStream)
                .collect(Collectors.toList());
    }

    /**
     * Creates a data point.
     *
     * @param node a node containing all the information required to build the data point
     * @return a data point
     */
    protected Optional<DataPoint<T>> asDataPoint(JsonNode node) {

        Optional<T> measure = newMeasure(node);

        return measure.map(m -> asDataPoint(node, m, newExternalId(node)));
    }

    /**
     * @param node a node containing all the information required to build the measure
     * @return a measure
     */
    protected abstract Optional<T> newMeasure(JsonNode node);

    /**
     * @param node a node containing all the information required to construct a unique identifier
     * @return a unique identifier for this node
     */
    private String newExternalId(JsonNode node) {

        String qualifier = asRequiredString(node, "activity");
        TimeFrame timeFrame = getTimeFrame(node).orElseThrow(IllegalStateException::new);

        return String.format("%s-%d", qualifier, timeFrame.getTimeInterval().getStartDateTime().toEpochSecond());
    }
}
