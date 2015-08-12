package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * The base class for mappers that translate Withings API responses with datapoints contained in an array to {@link
 * Measure} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */

public abstract class WithingsListDataPointMapper<T> extends WithingsDataPointMapper<T> {

    /**
     * Maps a JSON response with individual data points contained in a JSON array to a list of {@link DataPoint}
     * objects
     * with the appropriate measure. Splits individual nodes based on the name of the list node and then iteratively
     * maps the nodes in the list.
     *
     * @param responseNodes a list of a single JSON node containing the entire response from a Withings endpoint
     * @return a list of DataPoint objects of type T with the appropriate values mapped from the input JSON; because
     * these JSON objects are contained within an array in the input response, each object in that array will map into
     * an item in the list
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkNotNull(responseNodes.size() == 1, "A single response node is allowed per call.");
        JsonNode responseNodeBody = asRequiredNode(responseNodes.get(0), BODY_NODE_PROPERTY);
        List<DataPoint<T>> dataPoints = Lists.newArrayList();
        JsonNode listNode = asRequiredNode(responseNodeBody, getListNodeName());
        for (JsonNode listEntryNode : listNode) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;

    }

    /**
     * Abstract method to be implemented by subclasses mapping a JSON response node from the Withings API into a {@link
     * Measure} object of the appropriate type.
     *
     * @param node JSON response from the Withings API endpoint
     * @return a {@link DataPoint} object containing the target measure with the appropriate values from the JSON node
     * parameter, wrapped as an {@link Optional}
     */
    abstract Optional<DataPoint<T>> asDataPoint(JsonNode node);

    /**
     * @return the name of the list node used by this mapper
     */
    abstract String getListNodeName();
}
