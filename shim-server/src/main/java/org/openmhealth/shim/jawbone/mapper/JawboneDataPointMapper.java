package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    protected boolean isSensed(JsonNode listEntryNode) {

        return false; //TODO overwrite for physical activity
    }

}
