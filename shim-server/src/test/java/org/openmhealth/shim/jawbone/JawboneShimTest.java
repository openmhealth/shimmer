/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.jawbone;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.shim.ShimDataResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Danilo Bonilla
 */
public class JawboneShimTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("jawbone-moves.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ObjectMapper objectMapper = new ObjectMapper();

        JawboneShim.JawboneDataTypes.MOVES.getNormalizer();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class,
            JawboneShim.JawboneDataTypes.MOVES.getNormalizer());

        objectMapper.registerModule(module);

        ShimDataResponse response =
            objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response);

        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertTrue(map.containsKey(StepCount.SCHEMA_STEP_COUNT));

        List<StepCount> stepCounts = (List<StepCount>) map.get(StepCount.SCHEMA_STEP_COUNT);
        assertTrue(stepCounts != null && stepCounts.size() == 4);

        final String EXPECTED_HOURLY_TOTAL_TIMESTAMP = "2013112101";

        DateTime expectedTimeUTC =
            DateTimeFormat.forPattern("yyyyMMddHH").withZone(DateTimeZone.forID("America/Los_Angeles"))
                .parseDateTime(EXPECTED_HOURLY_TOTAL_TIMESTAMP);
        expectedTimeUTC = expectedTimeUTC.toDateTime(DateTimeZone.UTC);

        BigDecimal expectedDuration = new BigDecimal(793);
        Integer expectedSteps = 1603;

        Boolean found = false;
        for (StepCount sc : stepCounts) {
            if (sc.getEffectiveTimeFrame().getTimeInterval().getStartTime().equals(expectedTimeUTC)) {
                assertEquals(sc.getStepCount(), expectedSteps);
                assertEquals(sc.getEffectiveTimeFrame().getTimeInterval().getDuration().getValue(), expectedDuration);
                found = true;
            }
        }
        assertTrue(found);
    }
}
