package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * An exception thrown to indicate a problem when mapping a {@link JsonNode}.
 *
 * @author Emerson Farrugia
 */
public class JsonNodeMappingException extends RuntimeException {

    public JsonNodeMappingException(String message) {
        super(message);
    }
}
