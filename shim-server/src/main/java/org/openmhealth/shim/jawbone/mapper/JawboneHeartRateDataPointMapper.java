package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.TemporalRelationshipToPhysicalActivity.AT_REST;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/heartrate">API documentation</a>
 */
public class JawboneHeartRateDataPointMapper extends JawboneDataPointMapper<HeartRate> {

    @Override
    protected Optional<HeartRate> getMeasure(JsonNode listEntryNode) {

        Long restingHeartRate = asRequiredLong(listEntryNode, "resting_heartrate");

        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(restingHeartRate)
                .setTemporalRelationshipToPhysicalActivity(AT_REST);

        setEffectiveTimeFrame(heartRateBuilder, listEntryNode);

        return Optional.of(heartRateBuilder.build());
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode) {
        // TODO add reference
        return true; // Jawbone explicitly states that heart rate data only comes from their sensor-based devices
    }
}
