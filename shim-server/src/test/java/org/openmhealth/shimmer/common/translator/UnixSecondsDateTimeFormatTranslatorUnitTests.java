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

package org.openmhealth.shimmer.common.translator;

import com.google.common.collect.Lists;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.*;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Chris Schaefbauer
 */
public class UnixSecondsDateTimeFormatTranslatorUnitTests {

    UnixSecondsDateTimeFormatTranslator translator;

    public static final ZoneId EST = ZoneId.of("-05:00");
    public static final ZoneId CEST = ZoneId.of("+02:00");
    public static final ZoneId AUSTRALIA_ADELAIDE_TZ = ZoneId.of("Australia/Adelaide");

    @BeforeClass
    public void initializeTranslator() {

        translator = new UnixSecondsDateTimeFormatTranslator();
    }

    @DataProvider(name = "timeZoneCasesAndExpectedUnixSecondsTimeCombinations")
    protected Iterator<Object[]> allZoneIdAndExpectedRangeCombinationProvider() {

        return Lists.newArrayList(
                new Object[] {ZoneOffset.UTC, "1435708800"},
                new Object[] {ZoneOffset.MAX, "1435644000"},
                new Object[] {ZoneOffset.MIN, "1435773600"},
                new Object[] {AUSTRALIA_ADELAIDE_TZ, "1435674600"},
                new Object[] {EST, "1435726800"},
                new Object[] {CEST, "1435701600"}

        ).iterator();
    }

    @DataProvider(name = "timeOfDayCasesAndExpectedUnixSecondsTimeCombinations")
    protected Iterator<Object[]> allTimeOfDayAndDateTimeRangeCombinationProvider() {

        return Lists.newArrayList(
                new Object[] {LocalTime.MIDNIGHT, "1435708800"},
                new Object[] {LocalTime.NOON, "1435752000"},
                new Object[] {LocalTime.MAX, "1435795199"}

        ).iterator();
    }

    @Test(dataProvider = "timeZoneCasesAndExpectedUnixSecondsTimeCombinations")
    public void translatorShouldReturnCorrectUnixSecondsForZoneIds(ZoneId zoneId, String expectedUnixEpochSeconds) {

        String translatedEpochSecondsAsString = translator.translate(LocalDateTime.parse("2015-07-01T00:00:00").atZone(
                zoneId).toOffsetDateTime());

        assertThat(translatedEpochSecondsAsString, equalTo(expectedUnixEpochSeconds));
    }

    @Test(dataProvider = "timeOfDayCasesAndExpectedUnixSecondsTimeCombinations")
    public void translatorShouldReturnCorrectUnixSecondsForTimesOfDay(LocalTime timeOfDay,
            String expectedUnixEpochSeconds) {

        String translatedEpochSecondsAsString =
                translator.translate(LocalDateTime.of(LocalDate.parse("2015-07-01"), timeOfDay).atZone(
                        ZoneOffset.UTC).toOffsetDateTime());

        assertThat(translatedEpochSecondsAsString, equalTo(expectedUnixEpochSeconds));

    }
}
