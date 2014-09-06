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

package org.openmhealth.shim.withings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BloodPressure;
import org.openmhealth.schema.pojos.BloodPressureUnit;
import org.openmhealth.shim.ShimDataResponse;
import org.openmhealth.shim.ShimDataType;
import org.openmhealth.shim.runkeeper.RunkeeperShim;

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
public class WithingsShimTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testNormalizeBody() throws IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource("withings-body.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ObjectMapper objectMapper = new ObjectMapper();

        WithingsShim.WithingsDataType.BODY.getNormalizer();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class,
            WithingsShim.WithingsDataType.BODY.getNormalizer());

        objectMapper.registerModule(module);

        ShimDataResponse response =
            objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response);

        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertTrue(map.containsKey(BloodPressure.SCHEMA_BLOOD_PRESSURE));

        List<BloodPressure> bloodPressures = (List<BloodPressure>) map.get(BloodPressure.SCHEMA_BLOOD_PRESSURE);
        assertTrue(bloodPressures != null && bloodPressures.size() == 2);

        BloodPressure bloodPressure = bloodPressures.get(0);

        DateTime expectedDateTime = new DateTime(1408276657l*1000l, DateTimeZone.UTC);

        assertEquals(expectedDateTime,bloodPressure.getEffectiveTimeFrame().getDateTime());
        assertEquals(bloodPressure.getDiastolic().getValue(),new BigDecimal(75d));
        assertEquals(bloodPressure.getDiastolic().getUnit(), BloodPressureUnit.mmHg);
        assertEquals(bloodPressure.getSystolic().getValue(),new BigDecimal(133d));
        assertEquals(bloodPressure.getSystolic().getUnit(), BloodPressureUnit.mmHg);
    }

}
