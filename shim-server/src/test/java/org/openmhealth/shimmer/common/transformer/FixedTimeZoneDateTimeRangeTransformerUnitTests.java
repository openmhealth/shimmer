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

import com.google.common.collect.Range;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Emerson Farrugia
 */
public class FixedTimeZoneDateTimeRangeTransformerUnitTests extends DateTimeRangeTransformerUnitTests {

    @Test(dataProvider = "allZoneIdAndDateTimeRangeCombinations")
    public void transformRangeShouldReturnCorrectRange(ZoneId zoneId, Range<OffsetDateTime> inputRange)
            throws Exception {

        FixedTimeZoneDateTimeRangeTransformer transformer = new FixedTimeZoneDateTimeRangeTransformer(zoneId);
        Range<OffsetDateTime> transformedRange = transformer.transformRange(inputRange);

        assertThat(transformedRange.hasLowerBound(), equalTo(inputRange.hasLowerBound()));

        if (transformedRange.hasLowerBound()) {
            // FIXME convert zone ID to offset
            //            assertThat(transformedRange.lowerEndpoint().getOffset(), equalTo(zoneId));
            assertThat(transformedRange.lowerEndpoint().toInstant(), equalTo(inputRange.lowerEndpoint().toInstant()));
        }

        assertThat(transformedRange.hasUpperBound(), equalTo(inputRange.hasUpperBound()));

        if (transformedRange.hasUpperBound()) {
            // FIXME convert zone ID to offset
            //            assertThat(transformedRange.upperEndpoint().getOffset(), equalTo(zoneId));
            assertThat(transformedRange.upperEndpoint().toInstant(), equalTo(inputRange.upperEndpoint().toInstant()));
        }
    }
}