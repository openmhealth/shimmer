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
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalBoolean;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalOffsetDateTime;


/**
 * The base class for mappers that translate Moves API responses to data points.
 *
 * @author Jared Sieling
 * @author Emerson Farrugia
 */
public abstract class MovesDataPointMapper<T extends SchemaSupport> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Moves Resource API";

    protected static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssx");


    /**
     * @param node a node containing optional "startTime" and "endTime" fields
     * @return the equivalent time frame, if any
     */
    public Optional<TimeFrame> getTimeFrame(JsonNode node) {

        Optional<OffsetDateTime> startDateTime =
                asOptionalOffsetDateTime(node, "startTime", OFFSET_DATE_TIME_FORMATTER);

        Optional<OffsetDateTime> endDateTime = asOptionalOffsetDateTime(node, "endTime", OFFSET_DATE_TIME_FORMATTER);

        if (!startDateTime.isPresent() || !endDateTime.isPresent()) {
            return empty();
        }

        return Optional.of(new TimeFrame(ofStartDateTimeAndEndDateTime(startDateTime.get(), endDateTime.get())));
    }

    /**
     * @param node a node containing an optional "manual" field
     * @return the equivalent modality, if any
     */
    public Optional<DataPointModality> getModality(JsonNode node) {

        return asOptionalBoolean(node, "manual")
                .map(manual -> manual ? SELF_REPORTED : SENSED);
    }

    /**
     * Creates a data point.
     *
     * @param node a node containing all the information required to build the data point
     * @return a data point
     */
    protected DataPoint<T> asDataPoint(JsonNode node, T measure, String externalId) {

        DataPointAcquisitionProvenance.Builder acquisitionProvenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        Optional<DataPointModality> modality = getModality(node);
        modality.ifPresent(acquisitionProvenanceBuilder::setModality);

        DataPointAcquisitionProvenance acquisitionProvenance = acquisitionProvenanceBuilder.build();

        acquisitionProvenance.setAdditionalProperty("external_id", externalId);

        DataPointHeader header = new DataPointHeader.Builder(randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

        return new DataPoint<>(header, measure);
    }
}
