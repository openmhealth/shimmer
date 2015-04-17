package org.openmhealth.shim.fitbit;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.HeartRateUnitValue;
import org.openmhealth.shim.testing.ShimTestSupport;

public class FitbitShimTest extends ShimTestSupport {

    @Test
    public void testHeartRate() {

        List<HeartRate> datapoints = read("fitbit-heart.json", HeartRate.SCHEMA_HEART_RATE, FitbitShim.FitbitDataType.HEART.getNormalizer());
        assertEquals(6, datapoints.size());
        
        assertTimeFrameEquals("2014-07-13T03:30:00.000Z", datapoints.get(0).getEffectiveTimeFrame());
        assertHeartRateUnitEquals(50, HeartRateUnitValue.Unit.bpm, datapoints.get(0).getHeartRate());
    }
}
