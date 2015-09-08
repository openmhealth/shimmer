package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Google Fit "merged weight" endpoint responses
 * (derived:com.google.weight:com.google.android.gms:merge_weight) to {@link BodyWeight}
 * objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitBodyWeightDataPointMapper extends GoogleFitDataPointMapper<BodyWeight> {

    @Override
    public Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode listNode) {

        JsonNode valueList = asRequiredNode(listNode, getValueListNodeName());

        Double bodyWeightValue = asRequiredDouble(valueList.get(0), "fpVal");
        if (bodyWeightValue == 0) {
            return Optional.empty();
        }

        BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(KILOGRAM, bodyWeightValue));
        setEffectiveTimeFrameIfPresent(bodyWeightBuilder, listNode);

        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");

        BodyWeight bodyWeight = bodyWeightBuilder.build();
        return Optional.of(newDataPoint(bodyWeight, originDataSourceId.orElse(null)));
    }
}
