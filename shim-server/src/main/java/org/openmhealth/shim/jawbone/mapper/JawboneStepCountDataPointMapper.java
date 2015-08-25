package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.StepCount;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * @author Chris Schaefbauer
 */
public class JawboneStepCountDataPointMapper extends JawboneDataPointMapper<StepCount> {

    @Override
    protected Optional<StepCount> getMeasure(JsonNode listEntryNode) {

        long stepCountValue = asRequiredLong(listEntryNode, "details.steps");
        if (stepCountValue <= 0) {
            return Optional.empty();
        }
        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepCountValue);
        setEffectiveTimeFrame(stepCountBuilder, listEntryNode);
        StepCount stepCount = stepCountBuilder.build();
        return Optional.of(stepCount);
    }

    @Override
    protected boolean isSensed(JsonNode listEntryNode) {

        return true; // the moves endpoint, from which step count is derived, only contains data sensed by Jawbone
        // devices or by the Jawbone UP app
    }

}
