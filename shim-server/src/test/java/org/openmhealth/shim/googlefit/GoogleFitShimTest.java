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

package org.openmhealth.shim.googlefit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BodyHeight;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.DataPoint;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.HeartRateUnitValue;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;
import org.openmhealth.schema.pojos.generic.MassUnitValue.MassUnit;
import org.openmhealth.shim.ShimDataResponse;
import org.openmhealth.shim.googlefit.GoogleFitShim.GoogleFitDataTypes;

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
 * @author Eric Jain
 */
public class GoogleFitShimTest {

	@Test
    public void testActivity() throws IOException {
        List<Activity> dataPoints = getDataPoints("googlefit-activity.json", GoogleFitShim.GoogleFitDataTypes.ACTIVITY);
        assertTrue(dataPoints.size() == 3);
        assertEquals(DateTime.parse("2015-01-01T22:21:57.000Z"), dataPoints.get(0).getEffectiveTimeFrame().getTimeInterval().getStartTime());
        assertEquals(DateTime.parse("2015-01-01T23:29:49.000Z"), dataPoints.get(0).getEffectiveTimeFrame().getTimeInterval().getEndTime());
        assertEquals("Walking", dataPoints.get(0).getActivityName());
    }

	@Test
    public void testBodyHeight() throws IOException {
        List<BodyHeight> dataPoints = getDataPoints("googlefit-body-height.json", GoogleFitShim.GoogleFitDataTypes.BODY_HEIGHT);
        assertTrue(dataPoints.size() == 3);
        assertEquals(DateTime.parse("2014-10-28T20:30:22.587Z"), dataPoints.get(0).getEffectiveTimeFrame().getDateTime());
        assertEquals(new BigDecimal("1.850000023841858"), dataPoints.get(0).getLengthUnitValue().getValue());
        assertEquals(LengthUnit.m, dataPoints.get(0).getLengthUnitValue().getUnit());
    }

	@Test
    public void testBodyWeight() throws IOException {
        List<BodyWeight> dataPoints = getDataPoints("googlefit-body-weight.json", GoogleFitShim.GoogleFitDataTypes.BODY_WEIGHT);
        assertTrue(dataPoints.size() == 3);
        assertEquals(DateTime.parse("2015-02-13T00:00:00.000Z"), dataPoints.get(0).getEffectiveTimeFrame().getDateTime());
        assertEquals(new BigDecimal("72.0999984741211"), dataPoints.get(0).getMassUnitValue().getValue());
        assertEquals(MassUnit.kg, dataPoints.get(0).getMassUnitValue().getUnit());
    }

	@Test
    public void testHeartRate() throws IOException {
        List<HeartRate> dataPoints = getDataPoints("googlefit-heart-rate.json", GoogleFitShim.GoogleFitDataTypes.HEART_RATE);
        assertTrue(dataPoints.size() == 3);
        assertEquals(DateTime.parse("2015-01-30T15:37:48.186Z"), dataPoints.get(0).getEffectiveTimeFrame().getDateTime());
        assertEquals(Integer.valueOf(54), dataPoints.get(0).getHeartRate().getValue());
        assertEquals(HeartRateUnitValue.Unit.bpm, dataPoints.get(0).getHeartRate().getUnit());
    }

	@Test
    public void testStepCount() throws IOException {
        List<StepCount> dataPoints = getDataPoints("googlefit-step-count.json", GoogleFitShim.GoogleFitDataTypes.STEP_COUNT);
        assertTrue(dataPoints.size() == 1);
        assertEquals(DateTime.parse("2015-02-02T22:49:39.811Z"), dataPoints.get(0).getEffectiveTimeFrame().getTimeInterval().getStartTime());
        assertEquals(DateTime.parse("2015-02-02T23:25:20.811Z"), dataPoints.get(0).getEffectiveTimeFrame().getTimeInterval().getEndTime());
        assertEquals(Integer.valueOf(4146), dataPoints.get(0).getStepCount());
    }

	@Test
    public void testEmpty() throws IOException {
        List<StepCount> dataPoints = getDataPoints("googlefit-empty.json", GoogleFitShim.GoogleFitDataTypes.STEP_COUNT);
        assertTrue(dataPoints == null);
    }

    @SuppressWarnings("unchecked")
	private static <T extends DataPoint> List<T> getDataPoints(String resource, GoogleFitDataTypes type) throws IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        InputStream inputStream = url.openStream();

        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class, type.getNormalizer());

        objectMapper.registerModule(module);

        ShimDataResponse response = objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response);
        assertNotNull(response.getShim());

		Map<String, List<T>> map = (Map<String, List<T>>) response.getBody();
        return map != null ? map.get(type.getSchemaId()) : null;
	}
}
