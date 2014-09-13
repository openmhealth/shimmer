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

package org.openmhealth.shim.runkeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.ShimDataResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Danilo Bonilla
 */
public class RunkeeperShimTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testActivityNormalize() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("runkeeper-activity.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ObjectMapper objectMapper = new ObjectMapper();

        RunkeeperShim.RunkeeperDataType.ACTIVITY.getNormalizer();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class,
            RunkeeperShim.RunkeeperDataType.ACTIVITY.getNormalizer());

        objectMapper.registerModule(module);

        ShimDataResponse response =
            objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response);

        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertTrue(map.containsKey(Activity.SCHEMA_ACTIVITY));

        List<Activity> activities = (List<Activity>) map.get(Activity.SCHEMA_ACTIVITY);
        assertTrue(activities != null && activities.size() == 2);

        DateTimeFormatter dateFormatter =
            DateTimeFormat.forPattern("EEE, d MMM yyyy HH:mm:ss")
                .withZone(DateTimeZone.UTC);

        final String START_TIME_STRING = "Wed, 6 Aug 2014 04:49:00";
        DateTime expectedStartTimeUTC = dateFormatter.parseDateTime(START_TIME_STRING);

        Activity activity = activities.get(0);
        assertEquals(activity.getDistance().getValue(), new BigDecimal(6437.3760));
        assertEquals(activity.getDistance().getUnit(), LengthUnitValue.LengthUnit.m);
        assertEquals(activity.getActivityName(), "Rowing");
        assertEquals(
            activity.getEffectiveTimeFrame().getTimeInterval().getDateTime(),
            expectedStartTimeUTC);
        assertEquals(
            activity.getEffectiveTimeFrame().getTimeInterval().getDuration().getUnit(),
            DurationUnitValue.DurationUnit.sec);
        assertEquals(
            activity.getEffectiveTimeFrame().getTimeInterval().getDuration().getValue(),
            new BigDecimal(3600d));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWeightNormalize() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("runkeeper-weight.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ObjectMapper objectMapper = new ObjectMapper();

        RunkeeperShim.RunkeeperDataType.WEIGHT.getNormalizer();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class,
            RunkeeperShim.RunkeeperDataType.WEIGHT.getNormalizer());

        objectMapper.registerModule(module);

        ShimDataResponse response =
            objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response);

        Map<String, Object> map = (Map<String, Object>) response.getBody();
        assertTrue(map.containsKey(BodyWeight.SCHEMA_BODY_WEIGHT));

        List<BodyWeight> weights = (List<BodyWeight>) map.get(BodyWeight.SCHEMA_BODY_WEIGHT);
        assertTrue(weights != null && weights.size() == 2);

        DateTimeFormatter dateFormatter =
            DateTimeFormat.forPattern("EEE, d MMM yyyy HH:mm:ss")
                .withZone(DateTimeZone.UTC);

        final String START_TIME_STRING = "Sat, 9 Aug 2014 04:46:47";
        DateTime expectedStartTimeUTC = dateFormatter.parseDateTime(START_TIME_STRING);

        BodyWeight bodyWeight = weights.get(0);
        assertEquals(bodyWeight.getMassUnitValue().getValue().setScale(3, RoundingMode.HALF_DOWN),
            new BigDecimal(81.6466265999547d).setScale(3, RoundingMode.HALF_DOWN));
        assertEquals(bodyWeight.getMassUnitValue().getUnit(), MassUnitValue.MassUnit.kg);
        assertEquals(
            bodyWeight.getEffectiveTimeFrame().getDateTime(),
            expectedStartTimeUTC);
    }
}
