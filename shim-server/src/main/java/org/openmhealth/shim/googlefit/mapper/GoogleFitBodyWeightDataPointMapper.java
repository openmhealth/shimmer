package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged weight" endpoint responses
 * (derived:com.google.weight:com.google.android.gms:merge_weight) to {@link BodyWeight}
 * objects
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitBodyWeightDataPointMapper extends GoogleFitDataPointMapper<BodyWeight> {

    /**
     * Maps a JSON response node from the Google Fit API to a {@link BodyWeight} measure
     * @param listNode an individual datapoint from the array from the Google Fit response
     * @return a {@link DataPoint} object containing a {@link BodyWeight} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    public Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode listNode) {
        JsonNode valueList = asRequiredNode(listNode, getValueListNodeName());
        Double bodyWeightValue = asRequiredDouble(valueList.get(0), "fpVal");
        if(bodyWeightValue==0){
            return Optional.empty();
        }
        BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,bodyWeightValue));
        setEffectiveTimeFrameIfPresent(bodyWeightBuilder, listNode);

        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        BodyWeight bodyWeight = bodyWeightBuilder.build();
        return Optional.of(newDataPoint(bodyWeight,originDataSourceId.orElse(null)));
    }
}
