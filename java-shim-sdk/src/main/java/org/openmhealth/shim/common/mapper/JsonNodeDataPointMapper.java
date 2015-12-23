package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.SchemaSupport;


/**
 * A mapper that creates data points from one or more {@link JsonNode} inputs.
 *
 * @param <B> the body type of the data points to create
 * @author Emerson Farrugia
 */
public interface JsonNodeDataPointMapper<B extends SchemaSupport> extends DataPointMapper<B, JsonNode> {

}
