package org.openmhealth.schema.pojos.build;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.HeartRateUnitValue;
import org.openmhealth.schema.pojos.TemporalRelationshipToPhysicalActivity;
import org.openmhealth.schema.pojos.generic.TimeFrame;

import static org.openmhealth.schema.pojos.HeartRateUnitValue.Unit.bpm;

public class HeartRateBuilder implements SchemaPojoBuilder<HeartRate> {

    private HeartRate heartRate;

    public HeartRateBuilder() {
        heartRate = new HeartRate();
        heartRate.setEffectiveTimeFrame(new TimeFrame());
    }

    public HeartRateBuilder withTimeTaken(DateTime dateTime) {
        heartRate.getEffectiveTimeFrame().setDateTime(dateTime);
        return this;
    }

    public HeartRateBuilder withRate(Integer value) {
        HeartRateUnitValue heartRateUnitValue = new HeartRateUnitValue(value, bpm);
        heartRate.setHeartRate(heartRateUnitValue);
        return this;
    }

    public HeartRateBuilder withTimeTakenDescription(
        TemporalRelationshipToPhysicalActivity description) {
        heartRate.setTemporalRelationshipToPhysicalActivity(description);
        return this;
    }

    @Override
    public HeartRate build() {
        return heartRate;
    }
}
