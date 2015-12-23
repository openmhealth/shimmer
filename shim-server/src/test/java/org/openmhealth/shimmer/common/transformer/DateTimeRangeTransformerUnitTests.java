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

package org.openmhealth.shimmer.common.transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.testng.annotations.DataProvider;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Iterator;

import static com.google.common.collect.Range.*;
import static java.time.ZoneOffset.UTC;


/**
 * @author Emerson Farrugia
 */
public abstract class DateTimeRangeTransformerUnitTests {

    private static final OffsetDateTime JANUARY_FIRST_MIDNIGHT_UTC
            = LocalDate.of(2015, Month.JANUARY.getValue(), 1).atStartOfDay(UTC).toOffsetDateTime();

    private static final OffsetDateTime FEBRUARY_FIRST_MIDNIGHT_UTC = JANUARY_FIRST_MIDNIGHT_UTC.plusMonths(1);

    public static final Range<OffsetDateTime> ALL_TIME = all();
    public static final Range<OffsetDateTime> AFTER_DECEMBER = atLeast(JANUARY_FIRST_MIDNIGHT_UTC);
    public static final Range<OffsetDateTime> BEFORE_JANUARY = lessThan(JANUARY_FIRST_MIDNIGHT_UTC);
    public static final Range<OffsetDateTime> JANUARY =
            closedOpen(JANUARY_FIRST_MIDNIGHT_UTC, FEBRUARY_FIRST_MIDNIGHT_UTC);

    public static final ZoneId EST = ZoneId.of("-05:00");
    public static final ZoneId CEST = ZoneId.of("+02:00");
    public static final ZoneId AUSTRALIA_ADELAIDE_TZ = ZoneId.of("Australia/Adelaide");


    @DataProvider(name = "allZoneIdAndDateTimeRangeCombinations")
    protected Iterator<Object[]> allZoneIdAndDateTimeRangeCombinationProvider() {

        return Lists.newArrayList(
                new Object[] {EST, ALL_TIME},
                new Object[] {EST, AFTER_DECEMBER},
                new Object[] {EST, BEFORE_JANUARY},
                new Object[] {EST, JANUARY},
                new Object[] {CEST, ALL_TIME},
                new Object[] {CEST, AFTER_DECEMBER},
                new Object[] {CEST, BEFORE_JANUARY},
                new Object[] {CEST, JANUARY},
                new Object[] {AUSTRALIA_ADELAIDE_TZ, ALL_TIME},
                new Object[] {AUSTRALIA_ADELAIDE_TZ, AFTER_DECEMBER},
                new Object[] {AUSTRALIA_ADELAIDE_TZ, BEFORE_JANUARY},
                new Object[] {AUSTRALIA_ADELAIDE_TZ, JANUARY}).iterator();
    }
}
