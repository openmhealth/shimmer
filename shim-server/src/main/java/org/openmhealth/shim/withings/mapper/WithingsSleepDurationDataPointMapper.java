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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.SleepDuration2;
import org.openmhealth.schema.domain.omh.SleepEpisode;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalInteger;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;


/**
 * A mapper from Withings Sleep Summary endpoint responses (/sleep?action=getsummary) to {@link SleepDuration2}
 * objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_sleep_summary">Sleep Summary API documentation</a>
 */
public class WithingsSleepDurationDataPointMapper extends WithingsListDataPointMapper<SleepDuration2> {

    private WithingsSleepEpisodeDataPointMapper sleepEpisodeMapper = new WithingsSleepEpisodeDataPointMapper();

    @Override
    String getListNodeName() {
        return "series";
    }

    /**
     * Maps an individual list node from the array in the Withings sleep summary endpoint response into a {@link
     * SleepDuration2} data point.
     *
     * @param node activity node from the array "series" contained in the "body" of the endpoint response
     * @return a {@link SleepDuration2} data point
     */
    @Override
    Optional<DataPoint<SleepDuration2>> asDataPoint(JsonNode node) {

        Optional<DataPoint<SleepEpisode>> sleepEpisodeDataPoint = sleepEpisodeMapper.asDataPoint(node);

        if (!sleepEpisodeDataPoint.isPresent()) {
            return empty();
        }

        SleepEpisode sleepEpisode = sleepEpisodeDataPoint.get().getBody();

        SleepDuration2 sleepDuration =
                new SleepDuration2.Builder(sleepEpisode.getTotalSleepTime(), sleepEpisode.getEffectiveTimeFrame())
                        .build();

        sleepEpisode.getAdditionalProperties().forEach(sleepDuration::setAdditionalProperty);

        Optional<String> externalId = asOptionalLong(node, "id").map(Object::toString);

        WithingsDevice device = asOptionalInteger(node, "model")
                .flatMap(WithingsDevice::findByMagicNumber)
                .orElse(null);

        return Optional.of(newDataPoint(sleepDuration, externalId.orElse(null), true, device));
    }
}
