package org.openmhealth.shim.googlefit;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BodyHeight;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.HeartRateUnitValue;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;
import org.openmhealth.schema.pojos.generic.MassUnitValue.MassUnit;
import org.openmhealth.shim.testing.ShimTestSupport;

public class GoogleFitShimTest extends ShimTestSupport {

	@Test
    public void testActivity() {

	    List<Activity> datapoints = read("googlefit-activity.json", Activity.SCHEMA_ACTIVITY, GoogleFitShim.GoogleFitDataTypes.ACTIVITY.getNormalizer());
        assertEquals(3, datapoints.size());
        
        assertTimeFrameEquals("2015-01-01T22:21:57.000Z", "2015-01-01T23:29:49.000Z", datapoints.get(0).getEffectiveTimeFrame());
        assertEquals("Walking", datapoints.get(0).getActivityName());
    }

	@Test
    public void testBodyHeight() {

	    List<BodyHeight> datapoints = read("googlefit-body-height.json", BodyHeight.SCHEMA_BODY_HEIGHT, GoogleFitShim.GoogleFitDataTypes.BODY_HEIGHT.getNormalizer());
        assertEquals(3, datapoints.size());
        
        assertTimeFrameEquals("2014-10-28T20:30:22.587Z", datapoints.get(0).getEffectiveTimeFrame());
        assertLengthUnitEquals("1.850000023841858", LengthUnit.m, datapoints.get(0).getLengthUnitValue());
    }

	@Test
    public void testBodyWeight() {
        
	    List<BodyWeight> datapoints = read("googlefit-body-weight.json", BodyWeight.SCHEMA_BODY_WEIGHT, GoogleFitShim.GoogleFitDataTypes.BODY_WEIGHT.getNormalizer());
        assertEquals(3, datapoints.size());

        assertTimeFrameEquals("2015-02-13T00:00:00.000Z", datapoints.get(0).getEffectiveTimeFrame());
        assertMassUnitEquals("72.0999984741211", MassUnit.kg, datapoints.get(0).getMassUnitValue());
    }

	@Test
    public void testHeartRate() {

	    List<HeartRate> datapoints = read("googlefit-heart-rate.json", HeartRate.SCHEMA_HEART_RATE, GoogleFitShim.GoogleFitDataTypes.HEART_RATE.getNormalizer());
        assertEquals(3, datapoints.size());
        
        assertTimeFrameEquals("2015-01-30T15:37:48.186Z", datapoints.get(0).getEffectiveTimeFrame());
        assertHeartRateUnitEquals(54, HeartRateUnitValue.Unit.bpm, datapoints.get(0).getHeartRate());
    }

	@Test
    public void testStepCount() {
        
	    List<StepCount> datapoints = read("googlefit-step-count.json", StepCount.SCHEMA_STEP_COUNT, GoogleFitShim.GoogleFitDataTypes.STEP_COUNT.getNormalizer());
        assertEquals(1, datapoints.size());
        
        assertTimeFrameEquals("2015-02-02T22:49:39.811Z", "2015-02-02T23:25:20.811Z", datapoints.get(0).getEffectiveTimeFrame());
        assertEquals(Integer.valueOf(4146), datapoints.get(0).getStepCount());
    }

	@Test
    public void testEmpty() {
        List<StepCount> datapoints = read("googlefit-empty.json", StepCount.SCHEMA_STEP_COUNT, GoogleFitShim.GoogleFitDataTypes.STEP_COUNT.getNormalizer());
        assertNull(datapoints);
    }
}
