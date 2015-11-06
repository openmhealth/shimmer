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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DataPointHeader.Builder;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLocalDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * The base class for mappers that translate Fitbit API responses to {@link Measure} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class FitbitDataPointMapper<T extends SchemaSupport> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Fitbit Resource API";

    /**
     * Maps JSON response nodes from the Fitbit API into a list of {@link DataPoint} objects with the appropriate type.
     * <p>
     * Data points from the Fitbit API do not have any time zone information, so these mappers use UTC as the
     * timezone. There is currently no way to determine the correct time zone for a datapoint given the Fitbit API.</p>
     *
     * @param responseNodes the list of two json nodes - the first being the get-user-info response (from
     * user/<user-id>/profile) and the second being the specific data point of interest for the mapper
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; if JSON
     * objects are contained within an array in the input response, each item in that array will map into an item in the
     * list
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "FitbitDataPointMapper requires one response node.");

        JsonNode targetTypeNodeList = asRequiredNode(responseNodes.get(0), getListNodeName());

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode targetTypeNode : targetTypeNodeList) {
            asDataPoint(targetTypeNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    /**
     * Adds a {@link DataPointHeader} to a {@link Measure} object and wraps it as a {@link DataPoint}
     *
     * @param measure the body of the data point
     * @param externalId the identifier of the measure as recorded by the data provider in the JSON data-point
     * (optional)
     * @param <T> the measure type (e.g., StepCount, BodyMassIndex)
     * @return a {@link DataPoint} object containing the body and header that map values from Fitbit API response nodes
     * to schema objects
     */
    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, Long externalId) {

        DataPointAcquisitionProvenance acquisitionProvenance =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME).build();

        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }

        DataPointHeader header = new Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance).build();

        return new DataPoint<>(header, measure);
    }

    /**
     * Takes a Fitbit response JSON node, which contains a date and time property, and then maps them into an {@link
     * OffsetDateTime} object
     *
     * @return the date and time based on the "date" and "time" properties of the JsonNode parameter, wrapped as an
     * {@link Optional}
     */
    protected Optional<OffsetDateTime> combineDateTimeAndTimezone(JsonNode node) {

        Optional<LocalDateTime> dateTime = asOptionalLocalDateTime(node, "date", "time");
        Optional<OffsetDateTime> offsetDateTime = null;

        if (dateTime.isPresent()) {
            // FIXME fix the time zone offset to use the correct offset for the data point once it is fixed by Fitbit
            offsetDateTime = Optional.ofNullable(OffsetDateTime.of(dateTime.get(), ZoneOffset.UTC));

        }

        return offsetDateTime;
    }

    /**
     * Transforms a {@link LocalDateTime} object into an {@link OffsetDateTime} object with a UTC time zone
     *
     * @param dateTime local date and time for the Fitbit response JSON node
     * @return the date and time based on the input dateTime parameter
     */
    protected OffsetDateTime combineDateTimeAndTimezone(LocalDateTime dateTime) {

        // FIXME fix the time zone offset to use the appropriate offset for the data point once it is fixed by Fitbit
        return OffsetDateTime.of(dateTime, ZoneOffset.UTC);
    }

    /**
     * Implemented by subclasses to map a JSON response node from the Fitbit API into a {@link Measure} object of the
     * appropriate type
     *
     * @return a {@link DataPoint} object containing the target measure with the appropriate values from the JSON node
     * parameter, wrapped as an {@link Optional}
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode node);

    /**
     * @return the name of the list node used by this mapper
     */
    protected abstract String getListNodeName();
}
