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

import java.time.OffsetDateTime;
import java.time.ZoneId;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Sets the time zone of the date time range bounds to a fixed time zone.
 *
 * @author Emerson Farrugia
 */
public class FixedTimeZoneDateTimeRangeTransformer implements DateTimeRangeTransformer {

    private ZoneId timeZone;


    /**
     * @param timeZone the time zone to set the bounds to
     */
    public FixedTimeZoneDateTimeRangeTransformer(ZoneId timeZone) {

        checkNotNull(timeZone);

        this.timeZone = timeZone;
    }

    @Override
    public Range<OffsetDateTime> transformRange(Range<OffsetDateTime> inputRange) {

        if (inputRange.hasLowerBound() && inputRange.hasUpperBound()) {
            return Range.closedOpen(
                    toFixedTimeZone(inputRange.lowerEndpoint()),
                    toFixedTimeZone(inputRange.upperEndpoint()));
        }

        if (inputRange.hasLowerBound()) {
            return Range.atLeast(toFixedTimeZone(inputRange.lowerEndpoint()));
        }

        if (inputRange.hasUpperBound()) {
            return Range.lessThan(toFixedTimeZone(inputRange.upperEndpoint()));
        }

        return Range.all();

    }

    /**
     * @return a date time with the same instant as the specified date time, but in the fixed time zone
     */
    private OffsetDateTime toFixedTimeZone(OffsetDateTime dateTime) {

        return dateTime.atZoneSameInstant(timeZone).toOffsetDateTime();
    }
}
