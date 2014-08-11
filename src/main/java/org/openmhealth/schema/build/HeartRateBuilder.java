package org.openmhealth.schema.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.generic.TimeFrame;

public class HeartRateBuilder implements SchemaPojoBuilder<HeartRate> {

    private HeartRate heartRate;

    public HeartRateBuilder() {
        heartRate = new HeartRate();
        heartRate.setEffectiveTimeFrame(new TimeFrame());
    }

    public HeartRateBuilder setTimeTaken(DateTime dateTime) {
        heartRate.getEffectiveTimeFrame().setStartTime(dateTime);
        return this;
    }

    public HeartRateBuilder setRate(String value) {
        heartRate.setValue(new Integer(value));
        heartRate.setUnit(HeartRate.Unit.bpm);
        return this;
    }

    @Override
    public HeartRate build() {
        return heartRate;
    }
}
