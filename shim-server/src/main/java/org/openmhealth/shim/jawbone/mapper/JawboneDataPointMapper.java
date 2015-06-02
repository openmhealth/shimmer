package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * @author Emerson Farrugia
 */
public abstract class JawboneDataPointMapper<T> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Jawbone UP API";


    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        // all mapped Jawbone responses only require a single endpoint response
        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");

        // all mapped Jawbone responses contain a single list
        JsonNode listNode = asRequiredNode(responseNodes.get(0), getListNodeName());

        List<DataPoint<T>> dataPoints = new ArrayList<>();

        for (JsonNode listEntryNode : listNode) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    /**
     * @return the name of the list node used by this mapper
     */
    protected abstract String getListNodeName();

    /**
     * @param listEntryNode the list entry node
     * @return the data point mapped to from that entry, unless skipped
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode listEntryNode);

    /**
     * @param measure the body of the data point
     * @param sourceName the name of the source of the measure
     * @param externalId the identifier of the measure as recorded by the data provider
     * @param sensed true if the measure is sensed by a device, false if it's manually entered, null otherwise
     * @param <T> the measure type
     * @return a data point
     */
    // TODO extract this since it's identical to Misfit's
    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, String sourceName, String externalId,
            Boolean sensed) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(sourceName);

        if (sensed != null && sensed) {
            provenanceBuilder.setModality(SENSED);
        }

        DataPointAcquisitionProvenance acquisitionProvenance = provenanceBuilder.build();

        // TODO discuss the name of the external identifier, to make it clear it's the ID used by the source
        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }

        DataPointHeader header = new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

        return new DataPoint<>(header, measure);
    }
}
