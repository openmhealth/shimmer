package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.List;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * The base class for mappers that translate Withings API responses with datapoints wrapped in an array to {@link
 * Measure} objects
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */

public abstract class WithingsListDataPointMapper<T> extends WithingsDataPointMapper<T> {



    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        JsonNode responseNodeBody = asRequiredNode(responseNodes.get(0), BODY_NODE_PROPERTY);
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        JsonNode listNode = asRequiredNode(responseNodeBody, getListNodeName());
        for (JsonNode listEntryNode : listNode) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    abstract Optional<DataPoint<T>> asDataPoint(JsonNode node);

    abstract String getListNodeName();
}
