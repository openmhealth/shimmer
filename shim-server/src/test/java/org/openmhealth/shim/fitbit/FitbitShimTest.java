package org.openmhealth.shim.fitbit;

import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.testing.ShimTestSupport;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FitbitShimTest extends ShimTestSupport {

    @Test
    public void testWeight() {

        List<BodyWeight> datapoints = read("fitbit-weight.json", BodyWeight.SCHEMA_BODY_WEIGHT, FitbitShim.FitbitDataType.WEIGHT.getNormalizer());
        assertEquals(3, datapoints.size());
        
        assertTimeFrameEquals("2014-11-12T23:59:59.000Z", datapoints.get(0).getEffectiveTimeFrame());
        assertMassUnitEquals("49.4", MassUnitValue.MassUnit.kg, datapoints.get(0).getMassUnitValue());
    }


    @Test
    public void testStepCount() {

        List<StepCount> datapoints = read("fitbit-step-count.json", StepCount.SCHEMA_STEP_COUNT, FitbitShim.FitbitDataType.STEPS.getNormalizer());
        assertEquals(1, datapoints.size());
        
        assertTimeFrameEquals("2014-08-20T00:00:00.000Z", 1, DurationUnitValue.DurationUnit.d, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals(Integer.valueOf(4332), datapoints.get(0).getStepCount());
    }

    @Test
    public void testIntradayStepCount() {

        List<StepCount> datapoints = read("fitbit-step-count-1m.json", StepCount.SCHEMA_STEP_COUNT, FitbitShim.FitbitDataType.STEPS.getNormalizer());
        assertEquals(264, datapoints.size());
        
        assertTimeFrameEquals("2014-08-20T00:26:00.000Z", 1, DurationUnitValue.DurationUnit.min, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals(Integer.valueOf(7), datapoints.get(0).getStepCount());
    }

    @Test
    public void testActivity() {

        List<Activity> datapoints = read("fitbit-activity.json", Activity.SCHEMA_ACTIVITY, FitbitShim.FitbitDataType.ACTIVITY.getNormalizer());
        assertEquals(1, datapoints.size());
        
        assertTimeFrameEquals("2015-04-28T21:30:00.000Z", 1800000, DurationUnitValue.DurationUnit.ms, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals("Swimming", datapoints.get(0).getActivityName());
        assertLengthUnitEquals("0.48", LengthUnitValue.LengthUnit.km, datapoints.get(0).getDistance());
    }
}
