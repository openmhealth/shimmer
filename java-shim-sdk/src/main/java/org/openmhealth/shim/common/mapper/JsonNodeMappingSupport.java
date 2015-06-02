package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;


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
     * @throws MissingJsonNodeMappingException if the child node doesn't exist
     */
    public static JsonNode asRequiredNode(JsonNode parentNode, String path) {

        if (!parentNode.hasNonNull(path)) {
            throw new MissingJsonNodeMappingException(parentNode, path);
        }

        return parentNode.path(path);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param typeChecker the function to check if the type is compatible
     * @param converter the function to convert the node to a value
     * @param <T> the type of the value to convert to
     * @return the value of the child node
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't compatible
     */
    public static <T> T asRequiredValue(JsonNode parentNode, String path, Function<JsonNode, Boolean> typeChecker,
            Function<JsonNode, T> converter, Class<T> targetType) {

        JsonNode childNode = asRequiredNode(parentNode, path);

        if (!typeChecker.apply(childNode)) {
            throw new IncompatibleJsonNodeMappingException(parentNode, path, targetType);
        }

        return converter.apply(childNode);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't textual
     */
    public static String asRequiredString(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isTextual, JsonNode::textValue, String.class);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a long
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't an integer
     */
    public static Long asRequiredLong(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::longValue, Long.class);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a double
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't numeric
     */
    public static Double asRequiredDouble(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isNumber, JsonNode::doubleValue, Double.class);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a {@link LocalDate}
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't a date
     */
    // TODO overload with a DateTimeFormatter parameter
    public static LocalDate asRequiredLocalDate(JsonNode parentNode, String path) {

        String string = asRequiredString(parentNode, path);

        try {
            return LocalDate.parse(string);
        }
        catch (DateTimeParseException e) {
            throw new IncompatibleJsonNodeMappingException(parentNode, path, LocalDate.class, e);
        }
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as an {@link OffsetDateTime}
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't a date time
     */
    public static OffsetDateTime asRequiredOffsetDateTime(JsonNode parentNode, String path) {

        String string = asRequiredString(parentNode, path);

        try {
            return OffsetDateTime.parse(string);
        }
        catch (DateTimeParseException e) {
            throw new IncompatibleJsonNodeMappingException(parentNode, path, OffsetDateTime.class, e);
        }
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param typeChecker the function to check if the type is compatible
     * @param converter the function to convert the node to a value
     * @param <T> the type of the value to convert to
     * @return the value of the child node, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't compatible
     */
    public static <T> Optional<T> asOptionalValue(JsonNode parentNode, String path,
            Function<JsonNode, Boolean> typeChecker, Function<JsonNode, T> converter) {

        JsonNode childNode = parentNode.path(path);

        if (childNode.isMissingNode()) {
            logger.debug("A '{}' field wasn't found in node '{}'.", path, parentNode);
            return Optional.empty();
        }

        if (childNode.isNull()) {
            return Optional.empty();
        }

        if (!typeChecker.apply(childNode)) {
            logger.warn("The '{}' field in node '{}' isn't compatible.", path, parentNode);
            return Optional.empty();
        }

        return Optional.of(converter.apply(childNode));
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't textual
     */
    public static Optional<String> asOptionalString(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isTextual, JsonNode::textValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a boolean, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't boolean
     */
    public static Optional<Boolean> asOptionalBoolean(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isBoolean, JsonNode::booleanValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time
     */
    public static Optional<OffsetDateTime> asOptionalOffsetDateTime(JsonNode parentNode, String path) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return Optional.empty();
        }

        OffsetDateTime dateTime = null;

        try {
            dateTime = OffsetDateTime.parse(string.get());
        }
        catch (DateTimeParseException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid timestamp.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(dateTime);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param formatter the formatter to use to parse the value of the child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time
     */
    public static Optional<LocalDateTime> asOptionalLocalDateTime(JsonNode parentNode, String path,
            DateTimeFormatter formatter) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return Optional.empty();
        }

        LocalDateTime dateTime = null;

        try {
            dateTime = LocalDateTime.parse(string.get(), formatter);
        }
        catch (DateTimeParseException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid timestamp.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(dateTime);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time matching {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}
     */
    public static Optional<LocalDateTime> asOptionalLocalDateTime(JsonNode parentNode, String path) {

        return asOptionalLocalDateTime(parentNode, path, ISO_LOCAL_DATE_TIME);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a double, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't numeric
     */
    public static Optional<Double> asOptionalDouble(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isNumber, JsonNode::doubleValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a long, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't an integer
     */
    public static Optional<Long> asOptionalLong(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::longValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as an integer, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't an integer
     */
    public static Optional<Integer> asOptionalInteger(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::intValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a {@link ZoneId}, or an empty optional if the child doesn't exist or if
     * the value of the child node isn't a valid time zone
     */
    public static Optional<ZoneId> asOptionalZoneId(JsonNode parentNode, String path) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return Optional.empty();
        }

        ZoneId zoneId = null;

        try {
            zoneId = ZoneId.of(string.get());
        }
        catch (DateTimeException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid time zone.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(zoneId);
    }
}
