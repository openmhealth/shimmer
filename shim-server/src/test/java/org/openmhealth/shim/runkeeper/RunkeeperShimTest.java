package org.openmhealth.shim.runkeeper;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit;
import org.openmhealth.schema.pojos.generic.LengthUnitValue.LengthUnit;
import org.openmhealth.schema.pojos.generic.MassUnitValue.MassUnit;
import org.openmhealth.shim.testing.ShimTestSupport;

public class RunkeeperShimTest extends ShimTestSupport {

    @Test
    public void testActivity() {

        List<Activity> datapoints = read("runkeeper-activity.json", Activity.SCHEMA_ACTIVITY, RunkeeperShim.RunkeeperDataType.ACTIVITY.getNormalizer());
        assertEquals(2, datapoints.size());
        
        assertTimeFrameEquals("2014-08-06T04:49:00Z", 3600, DurationUnit.sec, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals("Rowing", datapoints.get(0).getActivityName());
        assertLengthUnitEquals("6437.37600000000020372681319713592529296875", LengthUnit.m, datapoints.get(0).getDistance());
    }

    @Test
    public void testBodyWeight() {
        
        List<BodyWeight> datapoints = read("runkeeper-weight.json", BodyWeight.SCHEMA_BODY_WEIGHT, RunkeeperShim.RunkeeperDataType.WEIGHT.getNormalizer());
        assertEquals(2, datapoints.size());

        assertTimeFrameEquals("2014-08-09T04:46:47Z", datapoints.get(0).getEffectiveTimeFrame());
        assertMassUnitEquals("81.6466265999547", MassUnit.kg, datapoints.get(0).getMassUnitValue());
    }
}
