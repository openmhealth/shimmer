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
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.gdata.util.common.base.Preconditions.checkArgument;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Moves Resource API <code>/user/storyline/daily</code> responses to data points.
 *
 * @author Emerson Farrugia
 * @author Jared Sieling
 * @see <a href="https://dev.moves-app.com/docs/api_storyline">API documentation</a>
 */
public abstract class MovesStorylineEndpointDataPointMapper<T extends SchemaSupport> extends MovesDataPointMapper<T> {

    private static final Logger logger = LoggerFactory.getLogger(MovesStorylineEndpointDataPointMapper.class);

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "A single response node is allowed per call.");

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        JsonNode segmentNodes = asRequiredNode(responseNodes.get(0), "segments");

        for (JsonNode segmentNode : segmentNodes) {

            MovesSegmentType segmentType = MovesSegmentType.getByJsonValue(asRequiredString(segmentNode, "type"));

            switch (segmentType) {
                case MOVE:

                    Optional<JsonNode> activityNodes = asOptionalNode(segmentNode, "activities");
                    if (activityNodes.isPresent()) {

                        for (JsonNode activityNode : activityNodes.get()) {
                            asDataPoint(segmentType, activityNode).ifPresent(dataPoints::add);
                        }
                    }
                    break;

                case PLACE:
                    asDataPoint(segmentType, segmentNode);
                    break;

                default:
                    logger.error("A Moves API response has unhandled segment type '{}'.", segmentType);
            }
        }

        return dataPoints;
    }

    /**
     * Creates a data point.
     *
     * @param segmentType the type of segment the node is contained in
     * @param node a node containing all the information required to build the data point
     * @return a data point
     */
    protected Optional<DataPoint<T>> asDataPoint(MovesSegmentType segmentType, JsonNode node) {

        Optional<T> measure = newMeasure(segmentType, node);

        if (!measure.isPresent()) {
            return empty();
        }

        DataPointAcquisitionProvenance.Builder acquisitionProvenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        Optional<DataPointModality> modality = getModality(node);
        modality.ifPresent(acquisitionProvenanceBuilder::setModality);

        DataPointAcquisitionProvenance acquisitionProvenance = acquisitionProvenanceBuilder.build();

        acquisitionProvenance.setAdditionalProperty("external_id", newExternalId(node));

        DataPointHeader header = new DataPointHeader.Builder(randomUUID().toString(), measure.get().getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

        return Optional.of(new DataPoint<>(header, measure.get()));
    }

    /**
     * @param segmentType the type of segment the node is contained in
     * @param node a node containing all the information required to build the measure
     * @return a measure
     */
    protected abstract Optional<T> newMeasure(MovesSegmentType segmentType, JsonNode node);

    /**
     * @param node a node containing all the information required to construct a unique identifier
     * @return a unique identifier for this node
     */
    protected abstract String newExternalId(JsonNode node);

    /**
     * @param node a node containing all the information required to construct a unique identifier
     * @param qualifierPath a path to a qualifier that disambiguates this node from another with the same time frame
     * @return a unique identifier for this node
     */
    protected String getExternalId(JsonNode node, String qualifierPath) {

        String qualifier = asRequiredString(node, qualifierPath);
        TimeFrame timeFrame = getTimeFrame(node);

        return String.format("%s-%d", qualifier, timeFrame.getTimeInterval().getStartDateTime().toEpochSecond());
    }
}
