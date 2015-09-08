package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;

import static java.lang.String.format;


/**
 * An exception thrown to indicate that the value of a {@link JsonNode} doesn't match an expected type.
 *
 * @author Emerson Farrugia
 */
public class IncompatibleJsonNodeMappingException extends JsonNodeMappingException {

    private Class<?> expectedType;

    /**
     * @param expectedType the expected type of the node value
     */
    public IncompatibleJsonNodeMappingException(JsonNode parentNode, String path, Class<?> expectedType) {
        super(parentNode, path);

        this.expectedType = expectedType;
    }

    /**
     * @param expectedType the expected type of the node value
     * @param cause the cause
     */
    public IncompatibleJsonNodeMappingException(JsonNode parentNode, String path, Class<?> expectedType,
            Throwable cause) {

        super(parentNode, path, cause);

        this.expectedType = expectedType;
    }

    public Class<?> getExpectedType() {
        return expectedType;
    }

    @Override
    public String getMessage() {
        String value = getParentNode().get(getPath()).asText();

        return format("The field '%s' with value '%s' doesn't have expected type '%s' in node '%s'.",
                getPath(), value, getExpectedType(), getParentNode());
    }
}
