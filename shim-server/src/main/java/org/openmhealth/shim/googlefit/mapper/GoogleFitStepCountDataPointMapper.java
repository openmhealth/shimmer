package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged step count delta" endpoint responses
 * (derived:com.google.step_count.delta:com.google.android.gms:merge_step_deltas) to {@link StepCount}
 * objects
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitStepCountDataPointMapper extends GoogleFitDataPointMapper<StepCount>{

    /**
     * Maps a JSON response node from the Google Fit API to a {@link StepCount}
     * @param listNode an individual datapoint from the array from the Google Fit response
     * @return a {@link DataPoint} object containing a {@link StepCount} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
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
