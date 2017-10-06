/*
 * Copyright 2017 Open mHealth
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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.SchemaSupport;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


public abstract class FitbitSleepMeasureDataPointMapper<T extends SchemaSupport> extends FitbitDataPointMapper<T> {

    /**
     * @param node a "levels.data" array node from a Fitbit sleep log response
     * @return the start time of the first non-awake entry, if any
     */
    protected Optional<OffsetDateTime> getSleepOnsetDateTime(JsonNode node) {

        return StreamSupport.stream(node.spliterator(), false)
                .filter(this::isAsleepEntry)
                .map(s -> asRequiredLocalDateTime(s, "dateTime"))
                .map(this::asOffsetDateTimeWithFakeUtcTimeZone)
                .findFirst();
    }

    /**
     * @param node a "levels.data" array entry node from a Fitbit sleep log response
     * @return true if the entry counts as asleep, false otherwise
     */
    private boolean isAsleepEntry(JsonNode node) {

        switch (asRequiredString(node, "level")) {
            case "wake": // for stages entries
            case "awake": // for classic entries
            case "restless": // for classic entries
                return false;
        }

        return true;
    }

    /**
     * @param node a "levels.data" array entry node from a Fitbit sleep log response
     * @return the end time of the last non-awake entry, if any
     */
    protected Optional<OffsetDateTime> getArisingDateTime(JsonNode node) {

        return StreamSupport.stream(node.spliterator(), false)
                .filter(this::isAsleepEntry)
                .map(s -> asRequiredLocalDateTime(s, "dateTime").plusSeconds(asRequiredInteger(s, "seconds")))
                .map(this::asOffsetDateTimeWithFakeUtcTimeZone)
                .reduce((first, second) -> second); // get last
    }
}
