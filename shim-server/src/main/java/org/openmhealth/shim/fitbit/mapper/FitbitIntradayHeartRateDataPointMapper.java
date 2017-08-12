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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.openmhealth.schema.domain.omh.TimeFrame;

import java.math.BigDecimal;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredBigDecimal;


/**
 * A mapper that translates responses from the Fitbit Resource API <code>activities/heart</code> endpoint into {@link
 * HeartRate} data points.
 *
 * @author Wallace Wadge
 * @see <a href="https://dev.fitbit.com/docs/heart-rate/#get-heart-rate-intraday-time-series">API documentation</a>
 */
public class FitbitIntradayHeartRateDataPointMapper extends FitbitIntradayDataPointMapper<HeartRate> {

    public FitbitIntradayHeartRateDataPointMapper(Integer intradayDataGranularityInMinutes) {
        super(intradayDataGranularityInMinutes);
    }

    @Override
    protected String getListNodeName() {
        return "activities-heart-intraday.dataset";
    }

    @Override
    public String getDateSummaryNodeName() {
        return "activities-heart";
    }

    @Override
    protected Optional<DataPoint<HeartRate>> asDataPoint(JsonNode listEntryNode) {

        BigDecimal heartRateValue = asRequiredBigDecimal(listEntryNode, "value");

        if (heartRateValue.intValue() == 0) {
            return Optional.empty();
        }

        TimeFrame effectiveTimeFrame = getTimeSeriesEntryEffectiveTimeFrame(listEntryNode);

        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(heartRateValue)
                .setEffectiveTimeFrame(effectiveTimeFrame);

        return Optional.of(
                newDataPoint(heartRateBuilder.build(), getTimeSeriesEntryExternalId(listEntryNode)));
    }
}
