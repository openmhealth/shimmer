package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
                "    \"empty\": null,\n" +
                "    \"date_time\": \"2014-01-01T12:15:04+02:00\"\n" +
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

        String string = asRequiredString(testNode, "string");

        assertThat(string, notNullValue());
        assertThat(string, equalTo("hi"));
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

        Long number = asRequiredLong(testNode, "integer");

        assertThat(number, notNullValue());
        assertThat(number, equalTo(2l));
    }

    @Test
    public void asOptionalStringShouldReturnEmptyOnMissingNode() {

        Optional<String> string = asOptionalString(testNode, "foo");

        assertThat(string, notNullValue());
        assertThat(string.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalStringShouldReturnEmptyOnNullNode() {

        Optional<String> string = asOptionalString(testNode, "empty");

        assertThat(string, notNullValue());
        assertThat(string.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalStringShouldReturnEmptyOnMismatchedNode() {

        Optional<String> string = asOptionalString(testNode, "number");

        assertThat(string, notNullValue());
        assertThat(string.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalStringShouldReturnStringWhenPresent() {

        Optional<String> string = asOptionalString(testNode, "string");

        assertThat(string, notNullValue());
        assertThat(string.isPresent(), equalTo(true));
        assertThat(string.get(), equalTo("hi"));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnMissingNode() {

        Optional<OffsetDateTime> dateTime = asOptionalOffsetDateTime(testNode, "foo");

        assertThat(dateTime, notNullValue());
        assertThat(dateTime.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnNullNode() {

        Optional<OffsetDateTime> dateTime = asOptionalOffsetDateTime(testNode, "empty");

        assertThat(dateTime, notNullValue());
        assertThat(dateTime.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnMismatchedNode() {

        Optional<OffsetDateTime> dateTime = asOptionalOffsetDateTime(testNode, "number");

        assertThat(dateTime, notNullValue());
        assertThat(dateTime.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnEmptyOnMalformedNode() {

        Optional<OffsetDateTime> dateTime = asOptionalOffsetDateTime(testNode, "string");

        assertThat(dateTime, notNullValue());
        assertThat(dateTime.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalOffsetDateTimeShouldReturnDateTimeWhenPresent() {

        Optional<OffsetDateTime> dateTime = asOptionalOffsetDateTime(testNode, "date_time");

        assertThat(dateTime, notNullValue());
        assertThat(dateTime.isPresent(), equalTo(true));
        assertThat(dateTime.get(), equalTo(OffsetDateTime.of(2014, 1, 1, 12, 15, 4, 0, ZoneOffset.ofHours(2))));
    }

    @Test
    public void asOptionalDoubleShouldReturnEmptyOnMissingNode() {

        Optional<Double> number = asOptionalDouble(testNode, "foo");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalDoubleShouldReturnEmptyOnNullNode() {

        Optional<Double> number = asOptionalDouble(testNode, "empty");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalDoubleShouldReturnEmptyOnMismatchedNode() {

        Optional<Double> number = asOptionalDouble(testNode, "string");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalDoubleShouldReturnDoubleWhenPresent() {

        Optional<Double> number = asOptionalDouble(testNode, "number");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(true));
        assertThat(number.get(), equalTo(2.3));
    }

    @Test
    public void asOptionalLongShouldReturnEmptyOnMissingNode() {

        Optional<Long> number = asOptionalLong(testNode, "foo");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLongShouldReturnEmptyOnNullNode() {

        Optional<Long> number = asOptionalLong(testNode, "empty");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLongShouldReturnEmptyOnMismatchedNode() {

        Optional<Long> number = asOptionalLong(testNode, "number");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(false));
    }

    @Test
    public void asOptionalLongShouldReturnLongWhenPresent() {

        Optional<Long> number = asOptionalLong(testNode, "integer");

        assertThat(number, notNullValue());
        assertThat(number.isPresent(), equalTo(true));
        assertThat(number.get(), equalTo(2L));
    }
}