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

package org.openmhealth.shim.ihealth.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * The base class for mappers that translate iHealth API responses to {@link DataPoint} objects.
 *
 * @author Chris Schaefbauer
 */
public abstract class IHealthDataPointMapper<T> implements DataPointMapper<T, JsonNode> {

    public static final String RESOURCE_API_SOURCE_NAME = "iHealth Resource API";
    public static final String DATA_SOURCE_MANUAL = "Manual";
    public static final String DATA_SOURCE_FROM_DEVICE = "FromDevice";

    /**
     * Maps a JSON response with individual data points contained in a JSON array to a list of {@link  DataPoint}
     * objects with the appropriate measure. Splits individual nodes and then iteratively maps the nodes in
     * the list.
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped iHealth responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode responseNode = responseNodes.get(0);

        Integer measureUnitMagicNumber = null;

        if (getMeasureUnitNodeName().isPresent()) {
            measureUnitMagicNumber = asRequiredInteger(responseNode, getMeasureUnitNodeName().get());
        }

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode listEntryNode : asRequiredNode(responseNode, getListNodeName())) {

            asDataPoint(listEntryNode, measureUnitMagicNumber).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    /**
     * Creates a data point header with information describing the data point created around the measure.
     * <p>
     * <p>Note: Additional properties within the header come from the iHealth API and are not defined by the data point
     * header schema. Additional properties are subject to change.</p>
     */
    protected DataPointHeader createDataPointHeader(JsonNode listNode, Measure measure) {

        DataPointAcquisitionProvenance.Builder acquisitionProvenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        asOptionalString(listNode, "DataSource").ifPresent(
                dataSource -> setAppropriateModality(dataSource, acquisitionProvenanceBuilder));

        DataPointAcquisitionProvenance acquisitionProvenance = acquisitionProvenanceBuilder.build();

        asOptionalString(listNode, "DataID")
                .ifPresent(externalId -> acquisitionProvenance.setAdditionalProperty("external_id",
                        externalId));

        asOptionalLong(listNode, "LastChangeTime").ifPresent(
                lastUpdatedInUnixSecs -> acquisitionProvenance.setAdditionalProperty("source_updated_date_time",
                        OffsetDateTime.ofInstant(Instant.ofEpochSecond(lastUpdatedInUnixSecs), ZoneId.of("Z"))));

        return new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

    }

    static protected void setEffectiveTimeFrameIfExists(JsonNode listNode, Measure.Builder builder) {

        Optional<Long> optionalOffsetDateTime = asOptionalLong(listNode, "MDate");

        if (optionalOffsetDateTime.isPresent()) {

            Optional<String> timeZoneString = asOptionalString(listNode, "TimeZone");

            if (timeZoneString.isPresent()) {

                OffsetDateTime offsetDateTimeCorrectOffset =
                        getDateTimeWithCorrectOffset(optionalOffsetDateTime.get(), timeZoneString.get());
                builder.setEffectiveTimeFrame(offsetDateTimeCorrectOffset);
            }

        }
    }

    /**
     * This method transforms the unix epoch second timestamps in iHealth responses, which are not in utc but instead
     * offset to the local time zone of the data point, into an {@link OffsetDateTime} with the correct date/time and
     * offset.
     */
    protected static OffsetDateTime getDateTimeWithCorrectOffset(Long dateTimeInUnixSecondsWithLocalTimeOffset,
            String timeZoneString) {

        // Since the timestamps are in local time, we can use the local date time provided by rendering the timestamp
        // in UTC, then translating that local time to the appropriate offset.
        OffsetDateTime offsetDateTimeFromOffsetInstant = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(dateTimeInUnixSecondsWithLocalTimeOffset),
                ZoneId.of("Z"));

        return offsetDateTimeFromOffsetInstant.toLocalDateTime().atOffset(ZoneOffset.of(timeZoneString));
    }

    static protected void setUserNoteIfExists(JsonNode listNode, Measure.Builder builder) {

        Optional<String> note = asOptionalString(listNode, "Note");

        if (note.isPresent() && !note.get().isEmpty()) {

            builder.setUserNotes(note.get());
        }
    }

    private void setAppropriateModality(String dataSourceValue, DataPointAcquisitionProvenance.Builder builder) {

        if (dataSourceValue.equals(DATA_SOURCE_FROM_DEVICE)) {
            builder.setModality(SENSED);
        }
        else if (dataSourceValue.equals(DATA_SOURCE_MANUAL)) {
            builder.setModality(SELF_REPORTED);
        }
    }

    /**
     * @return The name of the JSON array that contains the individual data points. This is different per endpoint.
     */
    protected abstract String getListNodeName();

    /**
     * @return The name of the JSON property whose value indicates the unit of measure used to render the values in
     * the response. This is different per endpoint and some endpoints do not provide any units, in which case, the
     * value should be an empty Optional.
     */
    protected abstract Optional<String> getMeasureUnitNodeName();

    /**
     * @param listEntryNode a single node from the
     * @param measureUnitMagicNumber The number representing the units used to render the response, according to
     * iHealth. This is retrieved from the main body of the response node. If the measure type does not use units, then
     * this value is null.
     * @return the data point mapped from the listEntryNode, unless skipped
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listEntryNode, Integer measureUnitMagicNumber);
}
