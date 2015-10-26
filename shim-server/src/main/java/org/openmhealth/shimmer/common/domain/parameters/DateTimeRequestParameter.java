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

package org.openmhealth.shimmer.common.domain.parameters;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @author Chris Schaefbauer
 */
public class DateTimeRequestParameter extends RequestParameter<OffsetDateTime> {

    // TODO unix time?
    // TODO how do we parse?
    private DateTimeFormatter dateTimeFormat;

    public DateTimeFormatter getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(DateTimeFormatter dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }
}
