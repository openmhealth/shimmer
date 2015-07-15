package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * Created by Chris Schaefbauer on 7/14/15.
 */
public class GoogleFitStepCountDataPointMapper extends GoogleFitDataPointMapper<StepCount>{

    @Override
    protected Optional<DataPoint<StepCount>> asDataPoint(JsonNode listNode) {

        JsonNode listValueNode = asRequiredNode(listNode,"value");
        long stepCountValue = asRequiredLong(listValueNode.get(0), "intVal");
        if(stepCountValue==0){
            return Optional.empty();
        }
        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepCountValue);
        setEffectiveTimeFrameIfPresent(stepCountBuilder,listNode);
        StepCount stepCount = stepCountBuilder.build();
        Optional<String> originSourceId = asOptionalString(listNode, "originDataSourceId");
        return Optional.of(newDataPoint(stepCount,originSourceId.orElse(null)));
    }
}
