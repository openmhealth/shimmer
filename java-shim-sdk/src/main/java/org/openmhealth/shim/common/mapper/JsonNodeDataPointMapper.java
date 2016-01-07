package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * A mapper that creates data points from one or more {@link JsonNode} inputs.
 *
 * @param <B> the body type of the data points to create
 * @author Emerson Farrugia
 */
public interface JsonNodeDataPointMapper<B> extends DataPointMapper<B, JsonNode> {

}
