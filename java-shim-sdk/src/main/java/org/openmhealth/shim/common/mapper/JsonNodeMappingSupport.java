package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static java.lang.String.format;


/**
 * A set of utility methods to help with mapping {@link JsonNode} objects.
 *
 * @author Emerson Farrugia
 */
public class JsonNodeMappingSupport {

    private static final Logger logger = LoggerFactory.getLogger(JsonNodeMappingSupport.class);


    /**
     * @param parentNode a parent node
     * @param path a path to a child node
     * @return the child node reached by traversing the path
     * @throws JsonNodeMappingException if the child node doesn't exist
     */
    public static JsonNode asRequiredNode(JsonNode parentNode, String path) {

        if (parentNode.hasNonNull(path)) {
            throw new JsonNodeMappingException(format("An '%s' field wasn't found in node '%s'.", path, parentNode));
        }

        return parentNode.path(path);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string
     * @throws JsonNodeMappingException if the child doesn't exist or if the value of the child node isn't textual
     */
    public static String asRequiredString(JsonNode parentNode, String path) {

        JsonNode childNode = asRequiredNode(parentNode, path);

        if (!childNode.isTextual()) {
            throw new JsonNodeMappingException(format("The '%s' field in node '%s' isn't textual.", path, parentNode));
        }

        return childNode.textValue();
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't textual
     */
    public static Optional<String> asOptionalString(JsonNode parentNode, String path) {

        JsonNode childNode = parentNode.path(path);

        if (childNode.isMissingNode()) {
            logger.warn("A '{}' field wasn't found in node '{}'.", path, parentNode);
            return Optional.empty();
        }

        if (childNode.isNull()) {
            return Optional.empty();
        }

        if (!childNode.isTextual()) {
            logger.warn("The '{}' field in node '{}' isn't textual.", path, parentNode);
            return Optional.empty();
        }

        return Optional.of(childNode.textValue());
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time
     */
    public static Optional<OffsetDateTime> asOptionalDateTime(JsonNode parentNode, String path) {

        JsonNode childNode = parentNode.path(path);

        if (childNode.isMissingNode()) {
            logger.warn("A '{}' field wasn't found in node '{}'.", path, parentNode);
            return Optional.empty();
        }

        if (childNode.isNull()) {
            return Optional.empty();
        }

        if (!childNode.isTextual()) {
            logger.warn("The '{}' field in node '{}' isn't textual.", path, parentNode);
            return Optional.empty();
        }

        OffsetDateTime dateTime = null;

        try {
            dateTime = OffsetDateTime.parse(childNode.textValue());
        }
        catch (DateTimeParseException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid timestamp.", parentNode, childNode,
                    e);
        }

        return Optional.ofNullable(dateTime);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a double, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't numeric
     */
    public static Optional<Double> asOptionalDouble(JsonNode parentNode, String path) {

        JsonNode childNode = parentNode.path(path);

        if (childNode.isMissingNode()) {
            logger.warn("A '{}' field wasn't found in node '{}'.", path, parentNode);
            return Optional.empty();
        }

        if (childNode.isNull()) {
            return Optional.empty();
        }

        if (!childNode.isNumber()) {
            logger.warn("The '{}' field in node '{}' isn't numeric.", path, parentNode);
            return Optional.empty();
        }

        return Optional.of(childNode.doubleValue());
    }
}
