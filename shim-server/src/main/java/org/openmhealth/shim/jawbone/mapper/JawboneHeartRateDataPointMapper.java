package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.openmhealth.schema.domain.omh.TemporalRelationshipToPhysicalActivity;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 */
public class JawboneHeartRateDataPointMapper extends JawboneDataPointMapper<HeartRate>{

    @Override
    protected Optional<HeartRate> getMeasure(JsonNode listEntryNode) {

        Long restingHeartRate = asRequiredLong(listEntryNode, "resting_heartrate");
        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(restingHeartRate).setTemporalRelationshipToPhysicalActivity(
                TemporalRelationshipToPhysicalActivity.AT_REST);
        setEffectiveTimeFrame(heartRateBuilder,listEntryNode);
        HeartRate heartRate = heartRateBuilder.build();
        return Optional.of(heartRate);
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode) {

        return true; // Jawbone explicitly states that heart rate data only comes from their sensor-based devices
    }
}
