package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged step count delta" endpoint responses
 * (derived:com.google.step_count.delta:com.google.android.gms:merge_step_deltas) to {@link StepCount}
 * objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitStepCountDataPointMapper extends GoogleFitDataPointMapper<StepCount> {

    @Override
    protected Optional<DataPoint<StepCount>> asDataPoint(JsonNode listNode) {

        JsonNode listValueNode = asRequiredNode(listNode, "value");
        long stepCountValue = asRequiredLong(listValueNode.get(0), "intVal");
        if (stepCountValue == 0) {
            return Optional.empty();
        }
        StepCount.Builder stepCountBuilder = new StepCount.Builder(stepCountValue);
        setEffectiveTimeFrameIfPresent(stepCountBuilder, listNode);
        StepCount stepCount = stepCountBuilder.build();
        Optional<String> originSourceId = asOptionalString(listNode, "originDataSourceId");
        return Optional.of(newDataPoint(stepCount, originSourceId.orElse(null)));
    }
}
