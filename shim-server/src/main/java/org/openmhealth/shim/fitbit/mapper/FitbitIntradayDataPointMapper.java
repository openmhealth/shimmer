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
import org.openmhealth.schema.domain.omh.SchemaSupport;
import org.openmhealth.schema.domain.omh.TimeFrame;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * TODO add Javadoc
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class FitbitIntradayDataPointMapper<T extends SchemaSupport> extends FitbitDataPointMapper<T> {

    // FIXME this shared state is a critical section if the mapper is reused
    private JsonNode responseNode;
    private Integer intradayDataGranularityInMinutes;


    public FitbitIntradayDataPointMapper(Integer intradayDataGranularityInMinutes) {
        this.intradayDataGranularityInMinutes = intradayDataGranularityInMinutes;
    }

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

    /**
     * @return the effective date of an intraday response
     */
    public LocalDate getEffectiveDate() {

        JsonNode dateSummaryNode = asRequiredNode(responseNode, getDateSummaryNodeName()).get(0);

        return asRequiredLocalDate(dateSummaryNode, "dateTime");
    }

    /**
     * @param timeSeriesEntryNode an entry node from an intraday time series
     * @return the effective start date time
     */
    protected OffsetDateTime getTimeSeriesEntryEffectiveStartDateTime(JsonNode timeSeriesEntryNode) {

        LocalDate effectiveDate = getEffectiveDate();
        LocalTime effectiveStartDateTime = asRequiredLocalTime(timeSeriesEntryNode, "time");

        return asOffsetDateTimeWithFakeUtcTimeZone(LocalDateTime.of(effectiveDate, effectiveStartDateTime));
    }

    /**
     * @param timeSeriesEntryNode an entry node from an intraday time series
     * @return an identifier that is unique to the specific time series entry, based on its effective time frame. This
     * identifier isn't unique across users or data types.
     */
    protected Long getTimeSeriesEntryExternalId(JsonNode timeSeriesEntryNode) {

        return getTimeSeriesEntryEffectiveStartDateTime(timeSeriesEntryNode).toEpochSecond();
    }

    /**
     * @param timeSeriesEntryNode an entry node from an intraday time series
     * @return the effective time frame
     */
    protected TimeFrame getTimeSeriesEntryEffectiveTimeFrame(JsonNode timeSeriesEntryNode) {

        return new TimeFrame(ofStartDateTimeAndDuration(
                getTimeSeriesEntryEffectiveStartDateTime(timeSeriesEntryNode),
                new DurationUnitValue(MINUTE, intradayDataGranularityInMinutes)));
    }

    /**
     * @return the name of the node which contains a summary for the requested date
     */
    public abstract String getDateSummaryNodeName();
}
