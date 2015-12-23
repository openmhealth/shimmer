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

package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * The base class for mappers that translate Jawbone API responses with data points contained in an array to {@link
 * Measure} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class JawboneDataPointMapper<T extends Measure> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Jawbone UP API";
    private static final int TIMEZONE_ENUM_INDEX_TZ = 1;
    private static final int TIMEZONE_ENUM_INDEX_START = 0;

    /**
     * Generates a {@link Measure} of the appropriate type from an individual list entry node with the correct values
     *
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     * @return the measure mapped to from that entry, unless skipped
     */
    protected abstract Optional<T> getMeasure(JsonNode listEntryNode);

    /**
     * Maps a JSON response with individual data points contained in the "items" JSON array to a list of {@link
     * DataPoint} objects with the appropriate measure. Splits individual nodes and then iteratively maps the nodes in
     * the list.
     *
     * @param responseNodes a list of a single JSON node containing the entire response from a Jawbone endpoint
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; because
     * these JSON objects are contained within an array in the input response, each object in that array will map into
     * an item in the list
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped Jawbone responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        // all mapped Jawbone responses contain a $.data.items list
        JsonNode dataNode = asRequiredNode(responseNodes.get(0), "data");
        JsonNode itemsNode = asRequiredNode(dataNode, "items");

        List<DataPoint<T>> dataPoints = new ArrayList<>();

        for (JsonNode itemNode : itemsNode) {
            Optional<T> measure = getMeasure(itemNode);
            if (measure.isPresent()) {

                dataPoints.add(new DataPoint<>(getHeader(itemNode, measure.get()), measure.get()));

            }
        }

        return dataPoints;
    }

    /**
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     * @return a {@link DataPointHeader} for containing the appropriate information based on the input parameters
     */
    protected DataPointHeader getHeader(JsonNode listEntryNode, T measure) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        if (isSensed(listEntryNode)) {
            provenanceBuilder.setModality(SENSED);
        }

        DataPointAcquisitionProvenance acquisitionProvenance = provenanceBuilder.build();

        asOptionalString(listEntryNode, "xid")
                .ifPresent(externalId -> acquisitionProvenance.setAdditionalProperty("external_id", externalId));
        // TODO discuss the name of the external identifier, to make it clear it's the ID used by the source

        asOptionalLong(listEntryNode, "time_updated").ifPresent(sourceUpdatedDateTime ->
                acquisitionProvenance.setAdditionalProperty("source_updated_date_time", OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(sourceUpdatedDateTime), ZoneId.of("Z"))));

        DataPointHeader header = new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

        // FIXME "shared" is never documented
        asOptionalBoolean(listEntryNode, "shared")
                .ifPresent(isShared -> header.setAdditionalProperty("shared", isShared));

        return header;
    }

    /**
     * @param builder a {@link Measure} builder
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     */
    protected void setEffectiveTimeFrame(T.Builder builder, JsonNode listEntryNode) {

        Optional<Long> optionalStartTime = asOptionalLong(listEntryNode, "time_created");
        Optional<Long> optionalEndTime = asOptionalLong(listEntryNode, "time_completed");

        if (optionalStartTime.isPresent() && optionalStartTime.get() != null && optionalEndTime.isPresent() &&
                optionalEndTime.get() != null) {

            ZoneId timeZoneForStartTime = getTimeZoneForTimestamp(listEntryNode, optionalStartTime.get());
            ZoneId timeZoneForEndTime = getTimeZoneForTimestamp(listEntryNode, optionalEndTime.get());

            OffsetDateTime startTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(optionalStartTime.get()),
                    timeZoneForStartTime);
            OffsetDateTime endTime =
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(optionalEndTime.get()), timeZoneForEndTime);

            builder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(startTime, endTime));
        }
        else if (optionalStartTime.isPresent() && optionalStartTime.get() != null) {

            ZoneId timeZoneForStartTime = getTimeZoneForTimestamp(listEntryNode, optionalStartTime.get());
            builder.setEffectiveTimeFrame(
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(optionalStartTime.get()), timeZoneForStartTime));
        }
    }

    /**
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     * @param unixEpochTimestamp unix epoch seconds timestamp from a time property in the list entry node
     * @return the appropriate {@link ZoneId} for the timestamp parameter based on the timezones contained within the
     * list entry node
     */
    static ZoneId getTimeZoneForTimestamp(JsonNode listEntryNode, Long unixEpochTimestamp) {

        Optional<JsonNode> optionalTimeZonesNode = asOptionalNode(listEntryNode, "details.tzs");
        Optional<JsonNode> optionalTimeZoneNode = asOptionalNode(listEntryNode, "details.tz");

        ZoneId zoneIdForTimestamp = ZoneOffset.UTC; // set default to Z in case problems with getting timezone

        if (optionalTimeZonesNode.isPresent() && optionalTimeZonesNode.get().size() > 0) {

            JsonNode timeZonesNode = optionalTimeZonesNode.get();

            if (timeZonesNode.size() == 1) {
                zoneIdForTimestamp = parseZone(timeZonesNode.get(0).get(TIMEZONE_ENUM_INDEX_TZ));
            }
            else {

                long currentLatestTimeZoneStart = 0;
                for (JsonNode timeZoneNodesEntry : timeZonesNode) {

                    long timeZoneStartTime = timeZoneNodesEntry.get(TIMEZONE_ENUM_INDEX_START).asLong();

                    if (unixEpochTimestamp >= timeZoneStartTime) {

                        if (timeZoneStartTime > currentLatestTimeZoneStart) { // we cannot guarantee the order of the
                            // "tzs" array and we need to find the latest timezone that started before our time

                            zoneIdForTimestamp = parseZone(timeZoneNodesEntry.get(TIMEZONE_ENUM_INDEX_TZ));
                            currentLatestTimeZoneStart = timeZoneStartTime;
                        }
                    }
                }
            }
        }
        else if (optionalTimeZoneNode.isPresent() && !optionalTimeZoneNode.get().isNull()) {

            zoneIdForTimestamp = parseZone(optionalTimeZoneNode.get());
        }

        return zoneIdForTimestamp;
    }

    // TODO clarify
    /**
     * Determines whether a data point is sensed. A false response does not guarantee that the data point is unsensed or
     * user entered.
     *
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     */
    protected boolean isSensed(JsonNode listEntryNode) {

        return false; // We make the conservative assumption that the data points are "not sensed", however
        // subclasses can override based on available information within different endpoint responses or API
        // documentation
    }

    /**
     * Translates a time zone descriptor from one of various representations (Olson, seconds offset, GMT offset) into a
     * {@link ZoneId}.
     *
     * @param timeZoneValueNode the value associated with a timezone property
     */
    static ZoneId parseZone(JsonNode timeZoneValueNode) {

        // default to UTC if timezone is not present
        if (timeZoneValueNode.isNull()) {
            return ZoneOffset.UTC;
        }

        // "-25200"
        if (timeZoneValueNode.asInt() != 0) {
            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(timeZoneValueNode.asInt());

            // TODO confirm if this is even necessary, since ZoneOffset is a ZoneId
            return ZoneId.ofOffset("GMT", zoneOffset);
        }

        // e.g., "GMT-0700" or "America/Los_Angeles"
        if (timeZoneValueNode.isTextual()) {
            return ZoneId.of(timeZoneValueNode.textValue());
        }

        throw new IllegalArgumentException(format("The time zone node '%s' can't be parsed.", timeZoneValueNode));
    }
}
