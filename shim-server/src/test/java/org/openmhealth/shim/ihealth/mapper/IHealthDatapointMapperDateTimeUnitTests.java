/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.ihealth.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.openmhealth.schema.domain.omh.TimeFrame;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openmhealth.shim.ihealth.mapper.IHealthDataPointMapper.getDateTimeAtStartOfDayWithCorrectOffset;
import static org.openmhealth.shim.ihealth.mapper.IHealthDataPointMapper.getEffectiveTimeFrameAsDateTime;


/**
 * @author Chris Schaefbauer
 */
public class IHealthDatapointMapperDateTimeUnitTests extends IHealthDataPointMapperUnitTests {

    HeartRate.Builder builder;

    @BeforeMethod
    public void initializeBuilder() {

        builder = new HeartRate.Builder(45);
    }

    @Test
    public void setEffectiveTimeFrameShouldNotAddTimeFrameWhenTimeZoneIsMissing() throws IOException {

        JsonNode timeInfoNode = createResponseNodeWithTimeZone(null);
        getEffectiveTimeFrameAsDateTime(timeInfoNode);

        assertThat(getEffectiveTimeFrameAsDateTime(timeInfoNode).isPresent(), is(false));
    }

    @Test
    public void setEffectiveTimeFrameShouldNotAddTimeFrameWhenTimeZoneIsEmpty() throws IOException {

        JsonNode timeInfoNode = createResponseNodeWithTimeZone("\"\"");

        assertThat(getEffectiveTimeFrameAsDateTime(timeInfoNode).isPresent(), is(false));
    }

    @Test
    public void setEffectiveTimeFrameReturnsTimeFrameInUtcWhenTimeZoneEqualsZero() throws IOException {

        testTimeFrameWhenItShouldBeSetCorrectly("0", "2015-11-17T18:24:23Z");
    }

    @Test
    public void setEffectiveTimeFrameShouldAddCorrectTimeFrameWhenTimeZoneIsPositiveInteger() throws IOException {

        testTimeFrameWhenItShouldBeSetCorrectly("1", "2015-11-17T18:24:23+01:00");
    }

    @Test
    public void setEffectiveTimeFrameShouldAddCorrectTimeFrameWhenTimeZoneIsNegativeInteger() throws IOException {

        testTimeFrameWhenItShouldBeSetCorrectly("-8", "2015-11-17T18:24:23-08:00");
    }

    @Test
    public void setEffectiveTimeFrameShouldAddCorrectTimeFrameWhenTimeZoneIsPositiveOffsetString() throws IOException {

        testTimeFrameWhenItShouldBeSetCorrectly("\"+0100\"", "2015-11-17T18:24:23+01:00");
    }


    @Test
    public void setEffectiveTimeFrameShouldAddCorrectTimeFrameWhenTimeZoneIsNegativeOffsetString() throws IOException {

        testTimeFrameWhenItShouldBeSetCorrectly("\"-0700\"", "2015-11-17T18:24:23-07:00");
    }

    @Test
    public void setEffectiveTimeFrameShouldAddTimeInUtcWhenTimeZoneIsZeroOffsetString() throws IOException {

        testTimeFrameWhenItShouldBeSetCorrectly("\"+0000\"", "2015-11-17T18:24:23Z");
    }

    @Test
    public void getDateTimeAtStartOfDayWithCorrectOffsetShouldReturnCorrectDateTimeWhenTimeIsAtStartOfDay() {

        long startOfDayEpochSecond = OffsetDateTime.parse("2015-11-12T00:00:00Z").toEpochSecond();

        OffsetDateTime dateTimeAtStartOfDay = getDateTimeAtStartOfDayWithCorrectOffset(startOfDayEpochSecond, "-0100");

        assertThat(dateTimeAtStartOfDay, equalTo(OffsetDateTime.parse("2015-11-12T00:00:00-01:00")));
    }

    @Test
    public void getDateTimeAtStartOfDayWithCorrectOffsetShouldReturnCorrectDateTimeWhenTimeIsAtEndOfDay() {

        long startOfDayEpochSecond = OffsetDateTime.parse("2015-11-12T23:59:59Z").toEpochSecond();

        OffsetDateTime dateTimeAtStartOfDay =
                getDateTimeAtStartOfDayWithCorrectOffset(startOfDayEpochSecond, "+0100");

        assertThat(dateTimeAtStartOfDay, equalTo(OffsetDateTime.parse("2015-11-12T00:00:00+01:00")));
    }

    public void testTimeFrameWhenItShouldBeSetCorrectly(String timezoneString, String expectedDateTime)
            throws IOException {

        JsonNode timeInfoNode = createResponseNodeWithTimeZone(timezoneString);
        Optional<TimeFrame> effectiveTimeFrameAsDateTime = getEffectiveTimeFrameAsDateTime(timeInfoNode);

        assertThat(effectiveTimeFrameAsDateTime.isPresent(), is(true));
        assertThat(effectiveTimeFrameAsDateTime.get().getDateTime(), equalTo(OffsetDateTime.parse(expectedDateTime)));
    }

    public JsonNode createResponseNodeWithTimeZone(String timezoneString) throws IOException {

        if (timezoneString == null) {
            return objectMapper.readTree("{\"MDate\": 1447784663,\n" +
                    "            \"Steps\": 100}\n");
        }
        else {
            return objectMapper.readTree("{\"MDate\": 1447784663,\n" +
                    "            \"Steps\": 100,\n" +
                    "\"TimeZone\": " + timezoneString + "}\n");
        }
    }
}
