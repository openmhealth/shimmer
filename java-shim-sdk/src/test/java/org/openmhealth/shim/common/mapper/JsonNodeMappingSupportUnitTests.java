package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * @author Emerson Farrugia
 */
public class JsonNodeMappingSupportUnitTests {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static JsonNode testNode;

    @BeforeClass
    public static void initializeTestNode() throws IOException {

        testNode = objectMapper.readTree("{\n" +
                "    \"number\": 2.3,\n" +
                "    \"integer\": 2,\n" +
                "    \"string\": \"hi\",\n" +
                "    \"boolean\": true,\n" +
                "    \"empty\": null,\n" +
                "    \"date_time\": \"2014-01-01T12:15:04+02:00\",\n" +
                "    \"date\": \"2014-01-01\",\n" +
                "    \"local_date_time\": \"2014-01-01T12:15:04\",\n" +
                "    \"custom_local_date_time\": \"Fri, 1 Aug 2014 06:53:05\",\n" +
                "    \"nested\": {\n" +
                "        \"empty\": null,\n" +
                "        \"string\": \"hi\"\n" +
                "    }\n" +
                "}");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredNodeShouldThrowExceptionOnMissingNode() {

        asRequiredNode(testNode, "foo");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredNodeShouldThrowExceptionOnNullNode() {

        asRequiredNode(testNode, "empty");
    }

    @Test
    public void asRequiredNodeShouldReturnNodeWhenPresent() {

        JsonNode node = asRequiredNode(testNode, "string");

        assertThat(node, notNullValue());
        assertThat(node.isMissingNode(), equalTo(false));
        assertThat(node.asText(), equalTo("hi"));
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredNodeShouldThrowExceptionOnMissingParentNode() {

        asRequiredNode(testNode, "foo.integer");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredNodeShouldThrowExceptionOnMissingChildNode() {

        asRequiredNode(testNode, "nested.foo");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredNodeShouldThrowExceptionOnNestedNullNode() {

        asRequiredNode(testNode, "nested.empty");
    }

    @Test
    public void asRequiredNodeShouldReturnNestedNodeWhenPresent() {

        JsonNode node = asRequiredNode(testNode, "nested.string");

        assertThat(node, notNullValue());
        assertThat(node.isMissingNode(), equalTo(false));
        assertThat(node.asText(), equalTo("hi"));
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredStringShouldThrowExceptionOnMissingNode() {

        asRequiredString(testNode, "foo");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredStringShouldThrowExceptionOnNullNode() {

        asRequiredString(testNode, "empty");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredStringShouldThrowExceptionOnMismatchedNode() {

        asRequiredString(testNode, "number");
    }

    @Test
    public void asRequiredStringShouldReturnStringWhenPresent() {

        String value = asRequiredString(testNode, "string");

        assertThat(value, notNullValue());
        assertThat(value, equalTo("hi"));
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredLongShouldThrowExceptionOnMissingNode() {

        asRequiredLong(testNode, "foo");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredLongShouldThrowExceptionOnNullNode() {

        asRequiredLong(testNode, "empty");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredLongShouldThrowExceptionOnMismatchedNode() {

        // since this is floating point
        asRequiredLong(testNode, "number");
    }

    @Test
    public void asRequiredLongShouldReturnLongWhenPresent() {

        Long value = asRequiredLong(testNode, "integer");

        assertThat(value, notNullValue());
        assertThat(value, equalTo(2l));
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredDoubleShouldThrowExceptionOnMissingNode() {

        asRequiredDouble(testNode, "foo");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredDoubleShouldThrowExceptionOnNullNode() {

        asRequiredDouble(testNode, "empty");
    }


    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredDoubleShouldThrowExceptionOnMismatchedNode() {

        asRequiredDouble(testNode, "string");
    }

    @Test
    public void asRequiredDoubleShouldReturnDoubleWhenPresent() {

        Double value = asRequiredDouble(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value, equalTo(2.3));
    }

    @Test
    public void asRequiredDoubleShouldReturnDoubleWhenIntegerIsPresent() {

        Double value = asRequiredDouble(testNode, "integer");

        assertThat(value, notNullValue());
        assertThat(value, equalTo(2d));
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredLocalDateShouldThrowExceptionOnMissingNode() {

        asRequiredLocalDate(testNode, "foo");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredLocalDateShouldThrowExceptionOnNullNode() {

        asRequiredLocalDate(testNode, "empty");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredLocalDateShouldThrowExceptionOnMismatchedNode() {

        asRequiredLocalDate(testNode, "number");
    }

    @Test
    public void asRequiredLocalDateShouldReturnLocalDateWhenPresent() {

        LocalDate value = asRequiredLocalDate(testNode, "date");

        assertThat(value, notNullValue());
        assertThat(value, equalTo(LocalDate.of(2014, 1, 1)));
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredOffsetDateTimeShouldThrowExceptionOnMissingNode() {

        asRequiredLocalDate(testNode, "foo");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredOffsetDateTimeShouldThrowExceptionOnNullNode() {

        asRequiredLocalDate(testNode, "empty");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asRequiredOffsetDateTimeShouldThrowExceptionOnMismatchedNode() {

        asRequiredLocalDate(testNode, "number");
    }

    @Test
    public void asRequiredOffsetDateTimeShouldReturnDateTimeWhenPresent() {

        OffsetDateTime value = asRequiredOffsetDateTime(testNode, "date_time");

        assertThat(value, notNullValue());
        assertThat(value, equalTo(OffsetDateTime.of(2014, 1, 1, 12, 15, 4, 0, ZoneOffset.ofHours(2))));
    }

    @Test
    public void asOptionalNodeShouldReturnEmptyOnMissingNode() {

        Optional<JsonNode> node = asOptionalNode(testNode, "foo");

        assertThat(node, notNullValue());
        assertThat(node.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalNodeShouldReturnEmptyOnOnNullNode() {

        Optional<JsonNode> node = asOptionalNode(testNode, "empty");

        assertThat(node, notNullValue());
        assertThat(node.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalNodeShouldReturnNodeWhenPresent() {

        Optional<JsonNode> node = asOptionalNode(testNode, "string");

        assertThat(node, notNullValue());
        assertThat(node.isPresent(), equalTo(true));
        assertThat(node.get().isMissingNode(), equalTo(false));
        assertThat(node.get().asText(), equalTo("hi"));
    }

    @Test
    public void asOptionalNodeShouldReturnEmptyOnOnMissingParentNode() {

        Optional<JsonNode> node = asOptionalNode(testNode, "foo.integer");

        assertThat(node, notNullValue());
        assertThat(node.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalNodeShouldReturnEmptyOnOnMissingChildNode() {

        Optional<JsonNode> node = asOptionalNode(testNode, "nested.foo");

        assertThat(node, notNullValue());
        assertThat(node.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalNodeShouldReturnEmptyOnOnNestedNullNode() {

        Optional<JsonNode> node = asOptionalNode(testNode, "nested.empty");

        assertThat(node, notNullValue());
        assertThat(node.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalNodeShouldReturnNestedNodeWhenPresent() {

        Optional<JsonNode> node = asOptionalNode(testNode, "nested.string");

        assertThat(node, notNullValue());
        assertThat(node.isPresent(), equalTo(true));
        assertThat(node.get().isMissingNode(), equalTo(false));
        assertThat(node.get().asText(), equalTo("hi"));
    }

    @Test
    public void asOptionalStringShouldReturnEmptyOnMissingNode() {

        Optional<String> value = asOptionalString(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalStringShouldReturnEmptyOnNullNode() {

        Optional<String> value = asOptionalString(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalStringShouldReturnEmptyOnMismatchedNode() {

        Optional<String> value = asOptionalString(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalStringShouldReturnStringWhenPresent() {

        Optional<String> value = asOptionalString(testNode, "string");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo("hi"));
    }

    @Test
    public void asOptionalBooleanShouldReturnEmptyOnMissingNode() {

        Optional<Boolean> value = asOptionalBoolean(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalBooleanShouldReturnEmptyOnNullNode() {

        Optional<Boolean> value = asOptionalBoolean(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalBooleanShouldReturnEmptyOnMismatchedNode() {

        Optional<Boolean> value = asOptionalBoolean(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalBooleanShouldReturnBooleanWhenPresent() {

        Optional<Boolean> value = asOptionalBoolean(testNode, "boolean");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(true));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnMissingNode() {

        Optional<OffsetDateTime> value = asOptionalOffsetDateTime(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnNullNode() {

        Optional<OffsetDateTime> value = asOptionalOffsetDateTime(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnMismatchedNode() {

        Optional<OffsetDateTime> value = asOptionalOffsetDateTime(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnMalformedNode() {

        Optional<OffsetDateTime> value = asOptionalOffsetDateTime(testNode, "string");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnDateTimeWhenPresent() {

        Optional<OffsetDateTime> value = asOptionalOffsetDateTime(testNode, "date_time");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(OffsetDateTime.of(2014, 1, 1, 12, 15, 4, 0, ZoneOffset.ofHours(2))));
    }

    @Test
    public void asOptionalLocalDateTimeShouldReturnEmptyOnMissingNode() {

        Optional<LocalDateTime> value = asOptionalLocalDateTime(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLocalDateTimeShouldReturnEmptyOnNullNode() {

        Optional<LocalDateTime> value = asOptionalLocalDateTime(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLocalDateTimeShouldReturnEmptyOnMismatchedNode() {

        Optional<LocalDateTime> value = asOptionalLocalDateTime(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLocalDateTimeShouldReturnEmptyOnMalformedNode() {

        Optional<LocalDateTime> value = asOptionalLocalDateTime(testNode, "string");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLocalDateTimeShouldReturnDateTimeWhenPresent() {

        Optional<LocalDateTime> value = asOptionalLocalDateTime(testNode, "local_date_time");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(LocalDateTime.of(2014, 1, 1, 12, 15, 4, 0)));
    }

    @Test
    public void asOptionalLocalDateTimeShouldReturnCustomDateTimeWhenPresent() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");

        Optional<LocalDateTime> value = asOptionalLocalDateTime(testNode, "custom_local_date_time", formatter);

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(LocalDateTime.of(2014, 8, 1, 6, 53, 5, 0)));
    }

    @Test
    public void asOptionalDoubleShouldReturnEmptyOnMissingNode() {

        Optional<Double> value = asOptionalDouble(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalDoubleShouldReturnEmptyOnNullNode() {

        Optional<Double> value = asOptionalDouble(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalDoubleShouldReturnEmptyOnMismatchedNode() {

        Optional<Double> value = asOptionalDouble(testNode, "string");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalDoubleShouldReturnDoubleWhenPresent() {

        Optional<Double> value = asOptionalDouble(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(2.3));
    }

    @Test
    public void asOptionalDoubleShouldReturnDoubleWhenIntegerIsPresent() {

        Optional<Double> value = asOptionalDouble(testNode, "integer");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(2d));
    }

    @Test
    public void asOptionalLongShouldReturnEmptyOnMissingNode() {

        Optional<Long> value = asOptionalLong(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLongShouldReturnEmptyOnNullNode() {

        Optional<Long> value = asOptionalLong(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLongShouldReturnEmptyOnMismatchedNode() {

        Optional<Long> value = asOptionalLong(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLongShouldReturnLongWhenPresent() {

        Optional<Long> value = asOptionalLong(testNode, "integer");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(2L));
    }

    @Test
    public void asOptionalIntegerShouldReturnEmptyOnMissingNode() {

        Optional<Integer> value = asOptionalInteger(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalIntegerShouldReturnEmptyOnNullNode() {

        Optional<Integer> value = asOptionalInteger(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalIntegerShouldReturnEmptyOnMismatchedNode() {

        Optional<Integer> value = asOptionalInteger(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalIntegerShouldReturnIntegerWhenPresent() {

        Optional<Integer> value = asOptionalInteger(testNode, "integer");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get(), equalTo(2));
    }

    @Test
    public void asOptionalBigDecimalShouldReturnBigDecimalForInteger() {

        Optional<BigDecimal> value = asOptionalBigDecimal(testNode, "integer");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get().intValue(), equalTo(2));
    }

    @Test
    public void asOptionalBigDecimalShouldReturnBigDecimalForDecimalValue() {

        Optional<BigDecimal> value = asOptionalBigDecimal(testNode, "number");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(true));
        assertThat(value.get().doubleValue(), equalTo(2.3));
    }

    @Test
    public void asOptionalBigDecimalShouldReturnEmptyOnMissingNode() {

        Optional<BigDecimal> value = asOptionalBigDecimal(testNode, "foo");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalBigDecimalShouldReturnEmptyOnNullNode() {

        Optional<BigDecimal> value = asOptionalBigDecimal(testNode, "empty");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalBigDecimalShouldReturnEmptyOnNonNumberNode() {

        Optional<BigDecimal> value = asOptionalBigDecimal(testNode, "string");

        assertThat(value, notNullValue());
        assertThat(value.isPresent(), equalTo(false));
    }
}