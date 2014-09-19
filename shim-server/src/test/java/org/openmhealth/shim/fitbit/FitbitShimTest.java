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

package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.shim.ShimDataResponse;
import org.openmhealth.shim.jawbone.JawboneShim;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Danilo Bonilla
 */
public class FitbitShimTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testNormalize() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("fitbit-heart.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ObjectMapper objectMapper = new ObjectMapper();

        FitbitShim.FitbitDataType.HEART.getNormalizer();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class,
            FitbitShim.FitbitDataType.HEART.getNormalizer());

        objectMapper.registerModule(module);

        ShimDataResponse response =
            objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response);

        assertNotNull(response.getShim());

        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertTrue(map.containsKey(HeartRate.SCHEMA_HEART_RATE));

        List<HeartRate> stepCounts = (List<HeartRate>) map.get(HeartRate.SCHEMA_HEART_RATE);
        assertTrue(stepCounts != null && stepCounts.size() == 6);
    }
}
