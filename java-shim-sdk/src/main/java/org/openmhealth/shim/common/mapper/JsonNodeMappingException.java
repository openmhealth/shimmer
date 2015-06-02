package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * An exception thrown to indicate a problem when mapping a {@link JsonNode}.
 *
 * @author Emerson Farrugia
 */
public class JsonNodeMappingException extends RuntimeException {

    private JsonNode parentNode;
    private String path;


    /**
     * @param message the detail message
     */
    public JsonNodeMappingException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause the cause
     */
    public JsonNodeMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param parentNode the parent of the node in question
     * @param path the path to the node
     */
    public JsonNodeMappingException(JsonNode parentNode, String path) {

        this.parentNode = parentNode;
        this.path = path;
    }

    /**
     * @param parentNode the parent of the node in question
     * @param path the path to the node
     * @param cause the cause
     */
    public JsonNodeMappingException(JsonNode parentNode, String path, Throwable cause) {
        super(cause);

        this.parentNode = parentNode;
        this.path = path;
    }

    public JsonNode getParentNode() {
        return parentNode;
    }

    public String getPath() {
        return path;
    }
}
