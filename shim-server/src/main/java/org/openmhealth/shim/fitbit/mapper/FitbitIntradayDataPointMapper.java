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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLocalDate;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * @author Chris Schaefbauer
 */
public abstract class FitbitIntradayDataPointMapper<T> extends FitbitDataPointMapper<T> {

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

    /**
     * Allows specific intraday activity measure mappers to access the date that the datapoint occured, which is stored
     * outside the individual list nodes
     */
    public Optional<LocalDate> getDateFromSummaryForDay() {

        JsonNode summaryForDayNode = asRequiredNode(responseNode, getSummaryForDayNodeName()).get(0);
        return asOptionalLocalDate(summaryForDayNode, "dateTime");
    }

    /**
     * @return the name of the summary list node which contains a data point with the dateTime field
     */
    public abstract String getSummaryForDayNodeName();
}
