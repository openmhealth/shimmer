package org.openmhealth.shim.jawbone;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.SleepDurationUnitValue;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit;
import org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;
import org.openmhealth.schema.pojos.generic.MassUnitValue.MassUnit;
import org.openmhealth.shim.testing.ShimTestSupport;

public class JawboneShimTest extends ShimTestSupport {

    @Test
    public void testMoves() {
        
        List<StepCount> datapoints = read("jawbone-moves.json", StepCount.SCHEMA_STEP_COUNT, JawboneShim.JawboneDataTypes.MOVES.getNormalizer());
        assertEquals(4, datapoints.size());

        assertTimeFrameEquals("2013-11-21T10:00:00Z", 246, DurationUnit.sec, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals(Integer.valueOf(455), datapoints.get(0).getStepCount());

        assertTimeFrameEquals("2013-11-21T09:00:00Z", 793, DurationUnit.sec, datapoints.get(1).getEffectiveTimeFrame());
        assertEquals(Integer.valueOf(1603), datapoints.get(1).getStepCount());
    }

    @Test
    public void testWorkouts() {
        
        List<Activity> datapoints = read("jawbone-workouts.json", Activity.SCHEMA_ACTIVITY, JawboneShim.JawboneDataTypes.WORKOUTS.getNormalizer());
        assertEquals(3, datapoints.size());

        assertTimeFrameEquals("2014-08-18T15:39:44Z", 300, DurationUnit.sec, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals("Walk", datapoints.get(0).getActivityName());
        assertLengthUnitEquals("2500", LengthUnit.m, datapoints.get(0).getDistance());
    }

    @Test
    public void testBodyWeight() {
        
        List<BodyWeight> datapoints = read("jawbone-body-weight.json", BodyWeight.SCHEMA_BODY_WEIGHT, JawboneShim.JawboneDataTypes.BODY.getNormalizer());
        assertEquals(2, datapoints.size());

        assertTimeFrameEquals("2014-11-05T17:00:11Z", datapoints.get(0).getEffectiveTimeFrame());
        assertMassUnitEquals("70.76041", MassUnit.kg, datapoints.get(0).getMassUnitValue());
    }

    @Test
    public void testSleep() {
        
        List<SleepDuration> datapoints = read("jawbone-sleep.json", SleepDuration.SCHEMA_SLEEP_DURATION, JawboneShim.JawboneDataTypes.SLEEP.getNormalizer());
        assertEquals(1, datapoints.size());

        assertTimeFrameEquals("2014-03-05T05:00:00.000Z", "2014-03-05T13:27:25.000Z", datapoints.get(0).getEffectiveTimeFrame());
        assertSleepDurationUnitEquals(507, SleepDurationUnitValue.Unit.min, datapoints.get(0).getSleepDurationUnitValue());
    }
}
