package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


// TODO rename. what's "getTimeZone"?
/**
 * @author Chris Schaefbauer
 */
public class JawboneGetTimeZoneUnitTests {

    ObjectMapper objectMapper = new ObjectMapper();

    /* Test JawboneDataPointMapper.parseZone */

    @Test
    public void parseZoneShouldReturnCorrectOlsonTimeZoneId() throws IOException {

        JsonNode testOlsonTimeZoneNode = objectMapper.readTree("\"America/New_York\"");

        ZoneId testZoneId = JawboneDataPointMapper.parseZone(testOlsonTimeZoneNode);
        ZoneId expectedZoneId = ZoneId.of("America/New_York");
        assertThat(testZoneId, equalTo(expectedZoneId));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200), testZoneId);
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2015-08-05T00:00:00-04:00");
        assertThat(testOffsetDateTime, equalTo(expectedOffsetDateTime));

    }

    @Test
    public void parseZoneShouldReturnCorrectSecondsOffsetTimeZoneId() throws IOException {

        JsonNode testSecondOffsetTimeZoneNode = objectMapper.readTree("-21600");

        ZoneId testZoneId = JawboneDataPointMapper.parseZone(testSecondOffsetTimeZoneNode);
        ZoneId expectedZoneId = ZoneId.of("-06:00");
        assertThat(testZoneId.getRules(), equalTo(expectedZoneId.getRules()));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200), testZoneId);
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2015-08-04T22:00:00-06:00");
        assertThat(testOffsetDateTime, equalTo(expectedOffsetDateTime));

        // Testing fractional for seconds offset
        testSecondOffsetTimeZoneNode = objectMapper.readTree("12600");
        testZoneId = JawboneDataPointMapper.parseZone(testSecondOffsetTimeZoneNode);
        expectedZoneId = ZoneId.of("+03:30");
        assertThat(testZoneId.getRules(), equalTo(expectedZoneId.getRules()));

    }

    @Test
    public void parseZoneShouldReturnCorrectGmtOffsetTimeZoneID() throws IOException {

        JsonNode testGmtOffsetTimeZoneNode = objectMapper.readTree("\"GMT-0600\"");

        ZoneId testZoneId = JawboneDataPointMapper.parseZone(testGmtOffsetTimeZoneNode);
        ZoneId expectedZoneId = ZoneId.of("-06:00");
        assertThat(testZoneId.getRules(), equalTo(expectedZoneId.getRules()));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200), testZoneId);
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2015-08-04T22:00:00-06:00");
        assertThat(testOffsetDateTime, equalTo(expectedOffsetDateTime));
    }

    @Test
    public void parseZoneShouldReturnCorrectTimeZoneForFractionalOffsetTimeZones() throws IOException {

        JsonNode fractionalTimeZoneWithName = objectMapper.readTree("\"Asia/Kathmandu\"");

        ZoneId testZoneId = JawboneDataPointMapper.parseZone(fractionalTimeZoneWithName);
        ZoneId expectedZoneId = ZoneId.of("+05:45");
        assertThat(testZoneId.getRules().toString(), equalTo(expectedZoneId.getRules()
                .toString())); //comparing toString because parsing fractional stores a bit more information so the
        // rules don't appear identical

        OffsetDateTime testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200), testZoneId);
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2015-08-05T09:45:00+05:45");
        assertThat(testOffsetDateTime, equalTo(expectedOffsetDateTime));

        JsonNode fractionalTimeZoneWithOffset = objectMapper.readTree("\"GMT+0330\"");
        testZoneId = JawboneDataPointMapper.parseZone(fractionalTimeZoneWithOffset);
        expectedZoneId = ZoneId.of("+03:30");
        assertThat(testZoneId.getRules(), equalTo(expectedZoneId.getRules()));

        testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200), testZoneId);
        expectedOffsetDateTime = OffsetDateTime.parse("2015-08-05T07:30:00+03:30");
        assertThat(testOffsetDateTime, equalTo(expectedOffsetDateTime));

    }

    /* Test JawboneDataPointMapper.getTimeZoneForTimestamp */

    @Test
    public void getTimeZoneForTimestampShouldReturnCorrectTimeZoneForResponseWithoutTimeZoneList() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"tz\": \"GMT-0200\"\n" +
                "},\n" +
                "\"time_created\": 1439990403,\n" +
                "\"time_updated\": 1439867504,\n" +
                "\"time_completed\": 1439994003\n" +
                "}");

        ZoneId timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1439990403L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("-02:00").getRules()));
    }

    @Test
    public void getTimeZoneForTimestampShouldReturnZTimeZoneWhenTimeZoneIsMissing() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "},\n" +
                "\"time_created\": 1439990403,\n" +
                "\"time_updated\": 1439867504,\n" +
                "\"time_completed\": 1439994003\n" +
                "}");

        ZoneId timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1439990403L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("Z").getRules()));
    }

    @Test
    public void getTimeZoneForTimestampShouldReturnZTimeZoneWhenTimeZoneIsNull() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"tz\": null\n" +
                "},\n" +
                "\"time_created\": 1439990403,\n" +
                "\"time_updated\": 1439867504,\n" +
                "\"time_completed\": 1439994003\n" +
                "}");

        ZoneId timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1439990403L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("Z").getRules()));
    }

    @Test
    public void getTimeZoneForTimestampShouldReturnCorrectTimeZonesForSingleTimeZoneInList() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"tz\": null,\n" +
                "\"tzs\": [\n" +
                "[\n" +
                "1439219760,\n" +
                "\"America/Denver\"\n" +
                "]\n" +
                "]" +
                "},\n" +
                "\"time_created\": 1439990403,\n" +
                "\"time_updated\": 1439867504,\n" +
                "\"time_completed\": 1439994003\n" +
                "}");

        ZoneId timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1439990403L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("America/Denver").getRules()));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.of(2015, 07, 25, 8, 20, 00, 00,
                timeZoneForTimestamp.getRules().getOffset(LocalDateTime.of(2015, 07, 25, 8, 20)));
        assertThat(testOffsetDateTime, equalTo(OffsetDateTime.parse("2015-07-25T08:20:00-06:00")));


    }

    @Test
    public void getTimeZoneForTimestampShouldReturnCorrectTimeZonesForMultipleTimeZones() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"tz\": null,\n" +
                "\"tzs\": [\n" +
                "[\n" +
                "1439219760,\n" +
                "\"America/Denver\"\n" +
                "],\n" +
                "[\n" +
                "1439494003,\n" +
                "\"America/Los_Angeles\"\n" +
                "],\n" +
                "[\n" +
                "1439994003,\n" +
                "\"Pacific/Honolulu\"\n" +
                "]\n" +
                "]" +
                "},\n" +
                "\"time_created\": 1439990403,\n" +
                "\"time_updated\": 1439867504,\n" +
                "\"time_completed\": 1439994003\n" +
                "}");

        // Testing early time after first time zone, but before second
        ZoneId timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1439221760L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("America/Denver").getRules()));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.of(2015, 07, 25, 8, 20, 00, 00,
                timeZoneForTimestamp.getRules().getOffset(LocalDateTime.of(2015, 07, 25, 8, 20)));
        assertThat(testOffsetDateTime, equalTo(OffsetDateTime.parse("2015-07-25T08:20:00-06:00")));

        //Testing time equal to last timezone change
        timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1439994003L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("Pacific/Honolulu").getRules()));

        testOffsetDateTime = OffsetDateTime.of(2015, 07, 25, 8, 20, 00, 00,
                timeZoneForTimestamp.getRules().getOffset(LocalDateTime.of(2015, 07, 25, 8, 20)));
        assertThat(testOffsetDateTime, equalTo(OffsetDateTime.parse("2015-07-25T08:20:00-10:00")));

        //Testing time after last timezone change
        timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1440004003L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("Pacific/Honolulu").getRules()));

        testOffsetDateTime = OffsetDateTime.of(2015, 07, 25, 8, 20, 00, 00,
                timeZoneForTimestamp.getRules().getOffset(LocalDateTime.of(2015, 07, 25, 8, 20)));
        assertThat(testOffsetDateTime, equalTo(OffsetDateTime.parse("2015-07-25T08:20:00-10:00")));

        //Testing time between second and last timezone
        timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1439894003L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("America/Los_Angeles").getRules()));

        testOffsetDateTime = OffsetDateTime.of(2015, 07, 25, 8, 20, 00, 00,
                timeZoneForTimestamp.getRules().getOffset(LocalDateTime.of(2015, 07, 25, 8, 20)));
        assertThat(testOffsetDateTime, equalTo(OffsetDateTime.parse("2015-07-25T08:20:00-07:00")));

    }

    @Test
    public void getTimeZoneForTimestampShouldReturnCorrectTimeZoneBehaviorForDaylightSavings() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"tz\": null,\n" +
                "\"tzs\": [\n" +
                "[\n" +
                "1425796620,\n" +
                "\"America/Denver\"\n" +
                "]\n" +
                "]" +
                "},\n" +
                "\"time_created\": 1425796620,\n" +
                "\"time_updated\": 1439867504,\n" +
                "\"time_completed\": 1425845420\n" +
                "}");

        ZoneId timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1425798620L);
        assertThat(timeZoneForTimestamp.getRules(), equalTo(ZoneId.of("America/Denver").getRules()));
        assertThat(timeZoneForTimestamp.getRules().getOffset(Instant.ofEpochSecond(1425798620)), equalTo(ZoneOffset.of(
                "-07:00")));
        OffsetDateTime testOffsetDateTime =
                OffsetDateTime.ofInstant(Instant.ofEpochSecond(1425796620), timeZoneForTimestamp);
        OffsetDateTime expectedDateTime = OffsetDateTime.parse("2015-03-07T23:37:00-07:00");
        assertThat(testOffsetDateTime, equalTo(expectedDateTime));

        timeZoneForTimestamp = JawboneDataPointMapper.getTimeZoneForTimestamp(testDateTimeNode, 1425845420L);
        assertThat(timeZoneForTimestamp.getRules().getOffset(Instant.ofEpochSecond(1425845420)),
                equalTo(ZoneOffset.of("-06:00")));


        testOffsetDateTime =
                OffsetDateTime.ofInstant(Instant.ofEpochSecond(1425845420), timeZoneForTimestamp);
        expectedDateTime = OffsetDateTime.parse("2015-03-08T14:10:20-06:00");
        assertThat(testOffsetDateTime, equalTo(expectedDateTime));

    }

}
