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

package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit merge heart rate endpoint responses (derived:com.google.heart_rate.bpm:com.google.android
 * .gms:merge_heart_rate_bpm) to {@link HeartRate} objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitHeartRateDataPointMapper extends GoogleFitDataPointMapper<HeartRate> {

    @Override
    protected Optional<DataPoint<HeartRate>> asDataPoint(JsonNode listNode) {

        JsonNode valueListNode = asRequiredNode(listNode, "value");
        double heartRateValue = asRequiredDouble(valueListNode.get(0), "fpVal");

        if (heartRateValue == 0) {
            return Optional.empty();
        }

        HeartRate.Builder measureBuilder = new HeartRate.Builder(heartRateValue);

        getOptionalTimeFrame(listNode).ifPresent(measureBuilder::setEffectiveTimeFrame);

        HeartRate heartRate = measureBuilder.build();
        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        return Optional.of(newDataPoint(heartRate, originDataSourceId.orElse(null)));
    }
}
