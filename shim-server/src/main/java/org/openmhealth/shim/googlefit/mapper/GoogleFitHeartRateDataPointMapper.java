package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * A mapper from Google Fit merge heart rate endpoint responses (derived:com.google.heart_rate.bpm:com.google.android
 * .gms:merge_heart_rate_bpm) to {@link HeartRate} objects
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitHeartRateDataPointMapper extends GoogleFitDataPointMapper<HeartRate> {

    /**
     * Maps a JSON response node from the Google Fit API to a {@link HeartRate} measure
     * @param listNode an individual datapoint from the array from the Google Fit response
     * @return a {@link DataPoint} object containing a {@link HeartRate} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    protected Optional<DataPoint<HeartRate>> asDataPoint(JsonNode listNode) {
        JsonNode valueListNode = asRequiredNode(listNode, "value");
        double heartRateValue = asRequiredDouble(valueListNode.get(0), "fpVal");
        if (heartRateValue == 0) {
            return Optional.empty();
        }
        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(heartRateValue);
        setEffectiveTimeFrameIfPresent(heartRateBuilder, listNode);
        HeartRate heartRate = heartRateBuilder.build();
        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");
        return Optional.of(newDataPoint(heartRate, originDataSourceId.orElse(null)));
    }
}
