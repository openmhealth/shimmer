package org.openmhealth.shim.ihealth;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.BloodGlucose;
import org.openmhealth.schema.pojos.BloodGlucoseUnitValue;
import org.openmhealth.schema.pojos.BloodPressure;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.HeartRateUnitValue;
import org.openmhealth.schema.pojos.SleepDuration;
import org.openmhealth.schema.pojos.StepCount;
import org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit;
import org.openmhealth.schema.pojos.generic.MassUnitValue.MassUnit;
import org.openmhealth.shim.testing.ShimTestSupport;

public class IHealthShimTest extends ShimTestSupport {

    @Test
    public void testActivity() {
        
        List<Activity> datapoints = read("ihealth-activity.json", Activity.SCHEMA_ACTIVITY, IHealthShim.IHealthDataTypes.ACTIVITY.getNormalizer());
        assertEquals(1, datapoints.size());
        
        assertTimeFrameEquals("2014-12-11T10:00:00Z", "2014-12-11T11:00:00Z", datapoints.get(0).getEffectiveTimeFrame());
        assertEquals("Run", datapoints.get(0).getActivityName());
    }

    @Test
    public void testBloodGlucose() {
        
        List<BloodGlucose> datapoints = read("ihealth-blood-glucose.json", BloodGlucose.SCHEMA_BLOOD_GLUCOSE, IHealthShim.IHealthDataTypes.BLOOD_GLUCOSE.getNormalizer());
        assertEquals(1, datapoints.size());

        assertTimeFrameEquals("2014-12-11T12:30:49Z", datapoints.get(0).getEffectiveTimeFrame());
        assertBloodGlucoseUnitEquals("80.5", BloodGlucoseUnitValue.Unit.mg_dL, datapoints.get(0).getBloodGlucose());
        assertEquals("testing", datapoints.get(0).getNotes());
    }

    @Test
    public void testBloodPressure() {

        List<BloodPressure> bloodPressure = read("ihealth-blood-pressure.json", BloodPressure.SCHEMA_BLOOD_PRESSURE, IHealthShim.IHealthDataTypes.BLOOD_PRESSURE.getNormalizer());
        assertEquals(1, bloodPressure.size());

        assertTimeFrameEquals("2014-12-11T08:55:56Z", bloodPressure.get(0).getEffectiveTimeFrame());
        assertBloodPressureEquals(100, 50, bloodPressure.get(0));
        assertEquals("testing", bloodPressure.get(0).getNotes());

        List<HeartRate> heartRate = read("ihealth-blood-pressure.json", HeartRate.SCHEMA_HEART_RATE, IHealthShim.IHealthDataTypes.BLOOD_PRESSURE.getNormalizer());
        assertEquals(1, heartRate.size());

        assertTimeFrameEquals("2014-12-11T08:55:56Z", heartRate.get(0).getEffectiveTimeFrame());
        assertHeartRateUnitEquals(60, HeartRateUnitValue.Unit.bpm, heartRate.get(0).getHeartRate());
    }

    @Test
    public void testBodyWeight() {
        
        List<BodyWeight> datapoints = read("ihealth-body-weight.json", BodyWeight.SCHEMA_BODY_WEIGHT, IHealthShim.IHealthDataTypes.BODY_WEIGHT.getNormalizer());
        assertEquals(2, datapoints.size());

        assertTimeFrameEquals("2014-12-10T07:38:10Z", datapoints.get(0).getEffectiveTimeFrame());
        assertMassUnitEquals("75.86718536514545", MassUnit.kg, datapoints.get(0).getMassUnitValue());
    }

    @Test
    public void testSleep() {
        
        List<SleepDuration> datapoints = read("ihealth-sleep.json", SleepDuration.SCHEMA_SLEEP_DURATION, IHealthShim.IHealthDataTypes.SLEEP.getNormalizer());
        assertEquals(1, datapoints.size());
        
        assertTimeFrameEquals("2014-12-10T23:00:00Z", "2014-12-11T07:00:00Z", datapoints.get(0).getEffectiveTimeFrame());
        assertEquals("zzz", datapoints.get(0).getNotes());
    }

    @Test
    public void testStepCount() {
        
        List<StepCount> datapoints = read("ihealth-step-count.json", StepCount.SCHEMA_STEP_COUNT, IHealthShim.IHealthDataTypes.STEP_COUNT.getNormalizer());
        assertEquals(1, datapoints.size());

        assertTimeFrameEquals("2014-12-11T07:00:00Z", 1, DurationUnit.d, datapoints.get(0).getEffectiveTimeFrame());
        assertEquals(Integer.valueOf(9000), datapoints.get(0).getStepCount());
    }
}
