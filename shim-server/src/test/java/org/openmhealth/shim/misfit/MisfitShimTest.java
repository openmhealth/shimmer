package org.openmhealth.shim.misfit;


import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.io.Resources;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.DataPoint;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.SleepDurationUnitValue;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit;
import org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;
import org.openmhealth.shim.ShimDataResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MisfitShimTest {

    @Test
    public void testMoves() {
        ShimDataResponse response = read("misfit-moves.json", MisfitShim.MisfitDataTypes.MOVES.getNormalizer());
        assertEquals("misfit", response.getShim());
        List<StepCount> stepCounts = get(StepCount.SCHEMA_STEP_COUNT, response);
        assertTrue(stepCounts.size() == 3);
        assertEquals(DateTime.parse("2015-04-13T00:00:00Z"), stepCounts.get(0).getEffectiveTimeFrame().getTimeInterval().getStartTime());
        assertEquals(DateTime.parse("2015-04-14T00:00:00Z"), stepCounts.get(0).getEffectiveTimeFrame().getTimeInterval().getEndTime());
        assertEquals(Integer.valueOf(26370), stepCounts.get(0).getStepCount());
    }

    @Test
    public void testActivities() {
        ShimDataResponse response = read("misfit-activities.json", MisfitShim.MisfitDataTypes.ACTIVITIES.getNormalizer());
        assertEquals("misfit", response.getShim());
        List<Activity> activities = get(Activity.SCHEMA_ACTIVITY, response);
        assertTrue(activities.size() == 3);
        assertEquals("Walking", activities.get(0).getActivityName());
        assertEquals(DateTime.parse("2015-04-13T11:46:00-07:00"), activities.get(0).getEffectiveTimeFrame().getTimeInterval().getStartTime());
        assertEquals(BigDecimal.valueOf(1140), activities.get(0).getEffectiveTimeFrame().getTimeInterval().getDuration().getValue());
        assertEquals(DurationUnit.sec, activities.get(0).getEffectiveTimeFrame().getTimeInterval().getDuration().getUnit());
        assertEquals(new BigDecimal("0.9371"), activities.get(0).getDistance().getValue());
        assertEquals(LengthUnit.km, activities.get(0).getDistance().getUnit());
    }

    @Test
    public void testSleep() {
        ShimDataResponse response = read("misfit-sleep.json", MisfitShim.MisfitDataTypes.SLEEP.getNormalizer());
        assertEquals("misfit", response.getShim());
        List<SleepDuration> sleepDurations = get(SleepDuration.SCHEMA_SLEEP_DURATION, response);
        assertTrue(sleepDurations.size() == 1);
        assertEquals(DateTime.parse("2015-04-14T21:35:05-07:00"), sleepDurations.get(0).getEffectiveTimeFrame().getTimestamp());
        assertEquals(BigDecimal.valueOf(215), sleepDurations.get(0).getSleepDurationUnitValue().getValue());
        assertEquals(SleepDurationUnitValue.Unit.min, sleepDurations.get(0).getSleepDurationUnitValue().getUnit());
    }

    private static ShimDataResponse read(String resource, JsonDeserializer<ShimDataResponse> deserializer) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(ShimDataResponse.class, deserializer);
            objectMapper.registerModule(module);
            URL url = Resources.getResource(resource);
            return objectMapper.readValue(url.openStream(), ShimDataResponse.class);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataPoint> List<T> get(String schema, ShimDataResponse response) {
        Map<String, List<T>> map = (Map<String, List<T>>) response.getBody();
        assertTrue(map.containsKey(schema));
        return map.get(schema);
    }
}
