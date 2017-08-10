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

package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DurationUnit;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration1;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/sleeps">API documentation</a>
 */
public class JawboneSleepDurationDataPointMapper extends JawboneDataPointMapper<SleepDuration1> {

    @Override
    protected Optional<SleepDuration1> getMeasure(JsonNode listEntryNode) {

        Long totalSleepSessionDuration = asRequiredLong(listEntryNode, "details.duration");
        Long totalTimeAwake = asRequiredLong(listEntryNode, "details.awake");

        SleepDuration1.Builder sleepDurationBuilder = new SleepDuration1.Builder(
                new DurationUnitValue(DurationUnit.SECOND, totalSleepSessionDuration - totalTimeAwake));

        setEffectiveTimeFrame(sleepDurationBuilder, listEntryNode);

        SleepDuration1 sleepDuration = sleepDurationBuilder.build();
        asOptionalLong(listEntryNode, "details.awakenings")
                .ifPresent(wakeUpCount -> sleepDuration.setAdditionalProperty("wakeup_count", wakeUpCount));

        return Optional.of(sleepDuration);
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode) {

        Optional<Long> optionalLightSleep = asOptionalLong(listEntryNode, "details.light");
        Optional<Long> optionalAwakeTime = asOptionalLong(listEntryNode, "details.awake");

        // Jawbone documentation states that sleep details, specifically awake and light sleep
        // values, are only recorded when sleep has been sensed by a Jawbone wearable. If these values are
        // zero, however, this does not guarantee that the data point is not sensed.
        if (optionalAwakeTime.isPresent() && optionalLightSleep.isPresent()) {
            if (optionalAwakeTime.get() > 0 || optionalLightSleep.get() > 0) {
                return true;
            }
        }

        return false;
    }
}
