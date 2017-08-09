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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.SchemaSupport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.ZoneOffset.UTC;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * TODO add Javadoc
 *
 * @author Chris Schaefbauer
 */
public abstract class FitbitIntradayDataPointMapper<T extends SchemaSupport> extends FitbitDataPointMapper<T> {

    // FIXME this shared state is a critical section if the mapper is reused
    private JsonNode responseNode;

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "A single response node is allowed per call.");

        responseNode = responseNodes.get(0);

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode listEntryNode : asRequiredNode(responseNode, getListNodeName())) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    protected Long getExternalIdFromTimeSeriesElementTimestamp(JsonNode listEntryNode) {
        Optional<LocalDate> dateFromParent = getDateFromSummaryForDay();

        if (dateFromParent.isPresent()) {
            final Optional<String> time = asOptionalString(listEntryNode, "time");

            if (time.isPresent()) {

                final OffsetDateTime effectiveTimeFrame =
                        dateFromParent.get().atTime(LocalTime.parse(time.get())).atOffset(UTC);
                return effectiveTimeFrame.toEpochSecond();
            }
        }

        return null;
    }

    /**
     * Allows specific intraday activity measure mappers to access the date that the datapoint occured, which is stored
     * outside the individual list nodes
     */
    // TODO discuss naming
    public Optional<LocalDate> getDateFromSummaryForDay() {

        JsonNode summaryForDayNode = asRequiredNode(responseNode, getSummaryForDayNodeName()).get(0);
        return asOptionalLocalDate(summaryForDayNode, "dateTime");
    }

    /**
     * @return the name of the summary list node which contains a data point with the dateTime field
     */
    // TODO discuss naming
    public abstract String getSummaryForDayNodeName();

    protected void setEffectiveTimeFrameFromTimeSeriesElementTimestamp(
            JsonNode listEntryNode,
            Measure.Builder builder) {

        Optional<LocalDate> dateFromParent = getDateFromSummaryForDay();

        if (dateFromParent.isPresent()) {

            // Set the effective time frame only if we have access to the date and time
            final Optional<String> time = asOptionalString(listEntryNode, "time");

            // We use 1 minute since the shim requests data at 1 minute granularity
            time.ifPresent(
                    s -> builder
                            .setEffectiveTimeFrame(
                                    ofStartDateTimeAndDuration(
                                            dateFromParent.get().atTime(LocalTime.parse(s)).atOffset(UTC),
                                            new DurationUnitValue(MINUTE, 1))));
        }
    }
}
