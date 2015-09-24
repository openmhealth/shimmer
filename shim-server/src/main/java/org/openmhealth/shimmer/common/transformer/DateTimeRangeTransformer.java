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


/**
 * @author Emerson Farrugia
 */
public interface DateTimeRangeTransformer {

    /**
     * @param inputRange the date time range to transform, assumed to be left-closed, right-open
     * @return the transformed date time range
     */
    Range<OffsetDateTime> transformRange(Range<OffsetDateTime> inputRange);
}
