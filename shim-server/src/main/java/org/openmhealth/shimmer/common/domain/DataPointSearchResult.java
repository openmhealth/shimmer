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

package org.openmhealth.shimmer.common.domain;

import org.openmhealth.schema.domain.omh.DataPoint;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/**
 * A bean that represents the result of a search for data points.
 *
 * @author Emerson Farrugia
 */
public class DataPointSearchResult {

    private List<DataPoint<?>> dataPoints = new ArrayList<>();
    private List<RequestResponsePair<?, ?>> requestResponsePairs = new ArrayList<>();


    /**
     * @return the list of data points matching the search criteria
     */
    @NotNull
    public List<DataPoint<?>> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<DataPoint<?>> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void addDataPoint(DataPoint<?> dataPoint) {
        dataPoints.add(dataPoint);
    }

    /**
     * @return the list of request-response pairs executed to effect the search
     */
    @NotNull
    public List<RequestResponsePair<?, ?>> getRequestResponsePairs() {
        return requestResponsePairs;
    }

    public void setRequestResponsePairs(List<RequestResponsePair<?, ?>> requestResponsePairs) {
        this.requestResponsePairs = requestResponsePairs;
    }

    public void addRequestResponsePair(RequestResponsePair<?, ?> requestResponsePair) {
        requestResponsePairs.add(requestResponsePair);
    }
}
