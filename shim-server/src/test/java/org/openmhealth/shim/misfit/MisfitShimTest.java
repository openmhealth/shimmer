package org.openmhealth.shim.misfit;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.SleepDurationUnitValue;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit;
import org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;
import org.openmhealth.shim.testing.ShimTestSupport;

public class MisfitShimTest extends ShimTestSupport {

    @Test
    public void testMoves() {
        
        List<StepCount> datapoints = read("misfit-moves.json", StepCount.SCHEMA_STEP_COUNT, MisfitShim.MisfitDataTypes.MOVES.getNormalizer());
        assertEquals(3, datapoints.size());

        assertTimeFrameEquals("2015-04-13T00:00:00Z", 1, DurationUnit.d, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals(Integer.valueOf(26370), datapoints.get(0).getStepCount());
    }

    @Test
    public void testActivities() {
        
        List<Activity> datapoints = read("misfit-activities.json", Activity.SCHEMA_ACTIVITY, MisfitShim.MisfitDataTypes.ACTIVITIES.getNormalizer());
        assertEquals(3, datapoints.size());
        
        assertTimeFrameEquals("2015-04-13T11:46:00-07:00", 1140, DurationUnit.sec, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals("Walking", datapoints.get(0).getActivityName());
        assertLengthUnitEquals("0.9371", LengthUnit.km, datapoints.get(0).getDistance());
    }

    @Test
    public void testSleep() {
        
        List<SleepDuration> datapoints = read("misfit-sleep.json", SleepDuration.SCHEMA_SLEEP_DURATION, MisfitShim.MisfitDataTypes.SLEEP.getNormalizer());
        assertEquals(1, datapoints.size());
        
        assertTimeFrameEquals("2015-04-14T21:35:05-07:00", datapoints.get(0).getEffectiveTimeFrame());
        assertSleepDurationUnitEquals(215, SleepDurationUnitValue.Unit.min, datapoints.get(0).getSleepDurationUnitValue());
    }
}
