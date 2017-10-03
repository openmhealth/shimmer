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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Streams;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.TimeFrame;
import org.openmhealth.schema.domain.omh.TimeInterval;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.ZoneOffset.UTC;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * A mapper from Withings Intraday Activity endpoint responses (/measure?action=getactivity) to measure
 * objects.
 * <p>
 * This mapper handles responses from an API request that requires special permissions from Withings. This special
 * activation can be requested from their <a href="http://oauth.withings .com/api/doc#api-Measure-get_intraday_measure">API
 * Documentation website</a></p>
 *
 * @author Emerson Farrugia
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_intraday_measure">Intrday Activity Measures API
 * documentation</a>
 */
public abstract class WithingsIntradayDataPointMapper<T extends Measure> extends WithingsDataPointMapper<T> {

    /**
     * Maps JSON response nodes from the intraday activities endpoint (measure?action=getintradayactivity) in the
     * Withings API into a list of data points.
     *
     * @param responseNodes a list of a single JSON node containing the entire response from the intraday activities
     * endpoint
     */
    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode bodyNode = asRequiredNode(responseNodes.get(0), "body");
        JsonNode seriesNode = asRequiredNode(bodyNode, "series");

        return Streams.stream(seriesNode.fields())
                .filter((e) -> e.getValue().hasNonNull(getMeasureValuePath()))
                .map(e -> {
                    MeasureTuple tuple = new MeasureTuple();

                    tuple.startDateTime = Instant.ofEpochSecond(Long.valueOf(e.getKey())).atOffset(UTC);
                    tuple.durationInSeconds = asRequiredLong(e.getValue(), "duration");
                    tuple.measureValue = asRequiredLong(e.getValue(), getMeasureValuePath());

                    return tuple;
                })
                .filter(p -> p.measureValue >= 0)
                .sorted()
                .map(p -> newMeasure(p.measureValue, p.getTimeFrame()))
                .map(m -> {
                    String externalId = String.valueOf(
                            m.getEffectiveTimeFrame().getTimeInterval().getStartDateTime().toEpochSecond());

                    return newDataPoint(m, externalId, true, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * @return the path of the node that contains the measure value
     */
    public abstract String getMeasureValuePath();


    class MeasureTuple implements Comparable<MeasureTuple> {

        public OffsetDateTime startDateTime;
        public long durationInSeconds;
        public long measureValue;

        public TimeFrame getTimeFrame() {
            return new TimeFrame(
                    TimeInterval.ofStartDateTimeAndDuration(startDateTime, SECOND.newUnitValue(durationInSeconds)));
        }

        @Override
        public int compareTo(MeasureTuple o) {
            return this.startDateTime.compareTo(o.startDateTime);
        }
    }

    public abstract T newMeasure(Long measureValue, TimeFrame effectiveTimeFrame);
}
