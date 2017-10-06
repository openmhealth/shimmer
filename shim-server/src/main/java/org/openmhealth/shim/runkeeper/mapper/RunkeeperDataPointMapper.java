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
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.UUID.randomUUID;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * An abstract mapper for building RunKeeper data points.
 *
 * @author Emerson Farrugia
 */
public abstract class RunkeeperDataPointMapper<T extends SchemaSupport> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Runkeeper HealthGraph API";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");


    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped RunKeeper responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        // all mapped RunKeeper responses contain a single list
        JsonNode listNode = asRequiredNode(responseNodes.get(0), getListNodeName());

        List<DataPoint<T>> dataPoints = new ArrayList<>();

        for (JsonNode listEntryNode : listNode) {

            // The Runkeeper HealthGraph API does not allow 3rd parties to write the utc_offset property in their
            // posts, so we filter out data in the HealthGraph API that does not come directly from Runkeeper. This
            // ensures that we can establish a time frame for each activity because we have utc_offset information.
            if (!listEntryNode.has("utc_offset")) {
                continue;
            }

            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    /**
     * @return the name of the list node used by this mapper
     */
    protected String getListNodeName() {
        return "items";
    }

    /**
     * @return a {@link DataPointHeader} for data points created from Runkeeper HealthGraph API responses
     */
    protected DataPointHeader getDataPointHeader(JsonNode itemNode, Measure measure) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        getModality(itemNode).ifPresent(provenanceBuilder::setModality);

        DataPointAcquisitionProvenance provenance = provenanceBuilder.build();

        asOptionalString(itemNode, "uri")
                .ifPresent(externalId -> provenance.setAdditionalProperty("external_id", externalId));

        DataPointHeader.Builder headerBuilder =
                new DataPointHeader.Builder(randomUUID().toString(), measure.getSchemaId())
                        .setAcquisitionProvenance(provenance);

        asOptionalInteger(itemNode, "userId").ifPresent(userId -> headerBuilder.setUserId(userId.toString()));

        return headerBuilder.build();
    }

    /**
     * @see <a href="http://billday.com/2013/04/09/validating-tracked-versus-manual-fitness-activities-using-the
     * -health-graph-api/">article on modality</a>
     */
    // TODO clarify source checks
    public Optional<DataPointModality> getModality(JsonNode itemNode) {

        String source = asOptionalString(itemNode, "source").orElse(null);
        String entryMode = asOptionalString(itemNode, "entry_mode").orElse(null);
        Boolean hasPath = asOptionalBoolean(itemNode, "has_path").orElse(null);

        if (entryMode != null && entryMode.equals("Web") && source != null && source.equalsIgnoreCase("RunKeeper")) {

            return Optional.of(DataPointModality.SELF_REPORTED);
        }

        if (source != null && source.equalsIgnoreCase("RunKeeper")
                && entryMode != null && entryMode.equals("API")
                && hasPath != null && hasPath) {

            return Optional.of(DataPointModality.SENSED);
        }

        return Optional.empty();
    }

    /**
     * @param node a JSON node optionally containing time frame properties
     */
    protected Optional<TimeFrame> getOptionalTimeFrame(JsonNode node) {

        Optional<LocalDateTime> localStartDateTime =
                asOptionalLocalDateTime(node, "start_time", DATE_TIME_FORMATTER);

        // RunKeeper doesn't support fractional time zones
        Optional<Integer> utcOffset = asOptionalInteger(node, "utc_offset");
        Optional<Double> durationInSeconds = asOptionalDouble(node, "duration");

        if (!localStartDateTime.isPresent() || !utcOffset.isPresent() || !durationInSeconds.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(asTimeFrame(localStartDateTime.get(), utcOffset.get(), durationInSeconds.get()));
    }

    private TimeFrame asTimeFrame(LocalDateTime localStartDateTime, int utcOffsetInHours, Double durationInSeconds) {

        OffsetDateTime startDateTime = localStartDateTime.atOffset(ZoneOffset.ofHours(utcOffsetInHours));

        return new TimeFrame(ofStartDateTimeAndDuration(startDateTime, SECOND.newUnitValue(durationInSeconds)));
    }

    /**
     * @param node a JSON node containing time frame properties
     */
    protected TimeFrame getTimeFrame(JsonNode node) {

        LocalDateTime localStartDateTime =
                asRequiredLocalDateTime(node, "start_time", DATE_TIME_FORMATTER);

        // RunKeeper doesn't support fractional time zones
        Integer utcOffset = asRequiredInteger(node, "utc_offset");
        Double durationInSeconds = asRequiredDouble(node, "duration");

        return asTimeFrame(localStartDateTime, utcOffset, durationInSeconds);
    }

    /**
     * @param listEntryNode the list entry node
     * @return the data point mapped to from that entry, unless skipped
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listEntryNode);
}
