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
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * @author Emerson Farrugia
 */
public abstract class JawboneDataPointMapper<T extends Measure> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Jawbone UP API";
    private static final int TIMEZONE_ENUM_INDEX_TZ = 1;
    private static final int TIMEZONE_ENUM_INDEX_START = 0;


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
                //asDataPoint(itemNode).ifPresent(dataPoints::add);
            }

        }

        return dataPoints;
    }

    /**
     * @param listEntryNode the list entry node
     * @return the data point mapped to from that entry, unless skipped
     */
    //protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listEntryNode);
    protected abstract Optional<T> getMeasure(JsonNode listEntryNode);

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
        asOptionalBoolean(listEntryNode, "shared")
                .ifPresent(isShared -> header.setAdditionalProperty("shared", isShared));

        return header;
    }

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
        else if(optionalStartTime.isPresent()&&optionalStartTime.get()!=null){

            ZoneId timeZoneForStartTime = getTimeZoneForTimestamp(listEntryNode, optionalStartTime.get());
            builder.setEffectiveTimeFrame(
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(optionalStartTime.get()), timeZoneForStartTime));
        }
    }

    static ZoneId getTimeZoneForTimestamp(JsonNode listEntryNode, Long unixEpochTimestamp) {

        Optional<JsonNode> optionalTimeZonesNode = asOptionalNode(listEntryNode, "details.tzs");
        Optional<JsonNode> optionalTimeZoneNode = asOptionalNode(listEntryNode, "details.tz");

        ZoneId zoneIdForTimestamp = ZoneId.of("Z"); // set default to Z in case problems with getting timezone

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

    protected boolean isSensed(JsonNode listEntryNode) {

        return false;
    }

    static ZoneId parseZone(JsonNode node) {

        ZoneId zone;
        if (node.isNull()) {
            zone = ZoneId.of("Z");
        }
        else if (node.asInt() != 0) { // "-25200"
            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(node.asInt());
            zone = ZoneId.ofOffset("GMT", zoneOffset);

        }
        else if (node.isTextual()) { // "GMT-0700" or "America/Los Angeles"
            zone = ZoneId.of(node.textValue());
        }
        else {
            throw new IllegalArgumentException("Can't parse time zone: <" + node + ">");
        }
        return zone;
    }

}
