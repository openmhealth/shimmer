package org.openmhealth.shim.microsoft.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalInteger;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;
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
 *
 */


/* @author wwadge */
public class MicrosoftHeartRateDataPointMapper extends MicrosoftDataPointMapper<HeartRate> {
    protected String getListNodeName() {
        return "summaries";
    }

    @Override
    protected Optional<DataPoint<HeartRate>> asDataPoint(JsonNode summaryNode) {
        checkNotNull(summaryNode);

        JsonNode heartRateSummary = asRequiredNode(summaryNode, "heartRateSummary");
        Optional<Integer> averageHeartRate = asOptionalInteger(heartRateSummary, "averageHeartRate");
        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(averageHeartRate.orElseGet(() -> 0));

        heartRateBuilder.setEffectiveTimeFrame(getStartTime(summaryNode));


        return Optional.of(newDataPoint(heartRateBuilder.build(), RESOURCE_API_SOURCE_NAME, null, null));
    }
}
