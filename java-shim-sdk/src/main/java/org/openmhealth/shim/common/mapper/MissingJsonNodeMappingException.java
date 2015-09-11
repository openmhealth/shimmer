package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * An exception thrown to indicate that a required {@link JsonNode} is missing.
 *
 * @author Emerson Farrugia
 */
public class MissingJsonNodeMappingException extends JsonNodeMappingException {

    public MissingJsonNodeMappingException(JsonNode parentNode, String path) {
        super(parentNode, path);
    }

    @Override
    public String getMessage() {
        return String.format("The required field '%s' wasn't found in node '%s'.", getPath(), getParentNode());
    }
}
