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

package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.SchemaSupport;
import org.openmhealth.shim.common.mapper.JsonNodeMappingException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.misfit.mapper.MisfitSleepMeasureDataPointMapper.SleepSegmentType.AWAKE;


/**
 * @author Emerson Farrugia
 */
public abstract class MisfitSleepMeasureDataPointMapper<T extends SchemaSupport> extends MisfitDataPointMapper<T> {

    protected List<SleepSegment> asSleepSegments(JsonNode node) {

        JsonNode sleepDetailsNode = asRequiredNode(node, "sleepDetails");

        List<SleepSegment> sleepSegments = new ArrayList<>();
        SleepSegment previousSegment = null;

        for (JsonNode sleepDetailSegmentNode : sleepDetailsNode) {

            SleepSegment sleepSegment = new SleepSegment();

            sleepSegment.setStartDateTime(asRequiredOffsetDateTime(sleepDetailSegmentNode, "datetime"));
            sleepSegment.setType(SleepSegmentType.getByMagicNumber(asRequiredInteger(sleepDetailSegmentNode, "value")));

            sleepSegments.add(sleepSegment);

            // finish constructing previous segment
            if (previousSegment != null) {
                previousSegment.setEndDateTime(sleepSegment.getStartDateTime());
            }

            previousSegment = sleepSegment;
        }

        // checking if the segment array is empty this way avoids compiler confusion later
        if (previousSegment == null) {
            throw new JsonNodeMappingException(format("The Misfit sleep node '%s' has no sleep details.", node));
        }

        // to calculate the end time of last segment, first determine the overall end time
        OffsetDateTime startDateTime = asRequiredOffsetDateTime(node, "startTime");
        Long totalDurationInSec = asRequiredLong(node, "duration");
        OffsetDateTime endDateTime = startDateTime.plusSeconds(totalDurationInSec);

        previousSegment.setEndDateTime(endDateTime);

        return sleepSegments;
    }


    class SleepSegment {

        private SleepSegmentType type;
        private OffsetDateTime startDateTime;
        private OffsetDateTime endDateTime;

        public SleepSegmentType getType() {
            return type;
        }

        public void setType(SleepSegmentType type) {
            this.type = type;
        }

        public OffsetDateTime getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(OffsetDateTime startDateTime) {
            this.startDateTime = startDateTime;
        }

        public OffsetDateTime getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(OffsetDateTime endDateTime) {
            this.endDateTime = endDateTime;
        }

        public long getDurationInSec() {
            return Duration.between(startDateTime, endDateTime).getSeconds();
        }
    }


    enum SleepSegmentType {

        AWAKE(1),
        SLEEP(2),
        DEEP_SLEEP(3);

        private int magicNumber;

        SleepSegmentType(int magicNumber) {
            this.magicNumber = magicNumber;
        }

        /**
         * @param magicNumber a magic number
         * @return the constant corresponding to the magic number
         */
        public static SleepSegmentType getByMagicNumber(Integer magicNumber) {

            for (SleepSegmentType constant : values()) {
                if (constant.magicNumber == magicNumber) {
                    return constant;
                }
            }

            throw new IllegalArgumentException(
                    format("A sleep segment type with value %d doesn't exist.", magicNumber));
        }
    }

    /**
     * @return the start time of the first non-awake entry, if any
     */
    protected Optional<OffsetDateTime> getSleepOnsetDateTime(List<SleepSegment> sleepSegments) {

        return sleepSegments.stream()
                .filter((segment) -> segment.getType() != AWAKE)
                .map(SleepSegment::getStartDateTime)
                .findFirst();
    }

    /**
     * @return the end time of the last non-awake entry, if any
     */
    protected Optional<OffsetDateTime> getArisingDateTime(List<SleepSegment> sleepSegments) {

        return sleepSegments.stream()
                .filter((segment) -> segment.getType() != AWAKE)
                .map(SleepSegment::getEndDateTime)
                .reduce((first, second) -> second); // get last
    }
}
