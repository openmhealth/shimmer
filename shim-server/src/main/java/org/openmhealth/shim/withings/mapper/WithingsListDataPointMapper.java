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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.SchemaSupport;
import org.openmhealth.schema.domain.omh.TimeFrame;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.LocalTime.MIDNIGHT;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredString;


/**
 * The base class for mappers that translate Withings API responses with datapoints contained in an array to {@link
 * Measure} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class WithingsListDataPointMapper<T extends Measure> extends WithingsDataPointMapper<T> {

    /**
     * Maps a JSON response with individual data points contained in a JSON array to a list of {@link DataPoint}
     * objects
     * with the appropriate measure. Splits individual nodes based on the name of the list node and then iteratively
     * maps the nodes in the list.
     *
     * @param responseNodes a list of a single JSON node containing the entire response from a Withings endpoint
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; because
     * these JSON objects are contained within an array in the input response, each object in that array will map into
     * an item in the list
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode listNode = asRequiredNode(responseNodes.get(0), BODY_NODE_PROPERTY + "." + getListNodeName());

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode listEntryNode : listNode) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    // TODO fix this
    /**
     * Abstract method to be implemented by subclasses mapping a JSON response node from the Withings API into a {@link
     * Measure} object of the appropriate type.
     *
     * @param node JSON response from the Withings API endpoint
     * @return a {@link DataPoint} object containing the target measure with the appropriate values from the JSON node
     * parameter, wrapped as an {@link Optional}
     */
    abstract Optional<DataPoint<T>> asDataPoint(JsonNode node);

    /**
     * @return the name of the list node used by this mapper
     */
    abstract String getListNodeName();

    protected TimeFrame getTimeFrame(JsonNode node) {

        LocalDate localDate = LocalDate.parse(asRequiredString(node, "date"));

        // We assume that timezone is the same for both the startdate and enddate timestamps, even though Withings only
        // provides the enddate timezone as the "timezone" property.
        ZoneId timeZoneId = ZoneId.of(asRequiredString(node, "timezone"));

        OffsetDateTime startDateTime = ZonedDateTime.of(localDate, MIDNIGHT, timeZoneId).toOffsetDateTime();

        return new TimeFrame(ofStartDateTimeAndEndDateTime(startDateTime, startDateTime.plusDays(1)));
    }
}
