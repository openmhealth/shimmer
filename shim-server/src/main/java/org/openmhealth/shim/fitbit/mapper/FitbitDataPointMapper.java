package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DataPointHeader.*;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;

/**
 * Created by Chris Schaefbauer on 6/11/15.
 */
public abstract class FitbitDataPointMapper<T> implements JsonNodeDataPointMapper<T> {
    public static final String RESOURCE_API_SOURCE_NAME = "Fitbit Resource API";

    /**
     *
     * @param responseNodes the list of two json nodes - the first being the get-user-info response and the second being the specific data point of interest for the mapper
     * @return
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {
        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 2, "FitbitDataPointMapper requires two response nodes - a response node from get-user-info and from the target datatype endpoint.");
        JsonNode firstNode = responseNodes.get(0);
        checkNotNull(firstNode.get("user"), "The first response node input to the FitbitDataPointMapper (index 0) should be the response from get-user-info");

        //TODO: Need to check that the nodes are in the appropriate order, or have a more robust approach to parsing the user-info and target-type nodes
        JsonNode userInfoNode = responseNodes.get(0);
        int utcOffsetMillis = asRequiredInteger(userInfoNode, "user.offsetFromUTCMillis");

        JsonNode targetTypeNodeList = asRequiredNode(responseNodes.get(1), getListNodeName());
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        for(JsonNode targetTypeNode:targetTypeNodeList){
            asDataPoint(targetTypeNode,utcOffsetMillis).ifPresent(dataPoints::add);
        }

        return dataPoints;

    }

    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, Long externalId) {


        DataPointAcquisitionProvenance acquisitionProvenance = new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME).build();
        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }
        DataPointHeader header = new Builder(UUID.randomUUID().toString(), measure.getSchemaId()).setAcquisitionProvenance(acquisitionProvenance).build();
        return new DataPoint<>(header,measure);
    }

    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode node, int offsetInMilliseconds);

    /**
     * @return the name of the list node used by this mapper
     */
    protected abstract String getListNodeName();
}
