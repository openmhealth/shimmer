package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.LengthUnit;
import org.openmhealth.schema.domain.omh.LengthUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * A mapper from Google Fit "merged height" endpoint responses
 * (derived:com.google.weight:com.google.android.gms:merge_height) to {@link BodyHeight}
 * objects.
 *
 * @author Chris Schaefbauer
 * @see <a href="https://developers.google.com/fit/rest/v1/data-types">Google Fit Data Type Documentation</a>
 */
public class GoogleFitBodyHeightDataPointMapper extends GoogleFitDataPointMapper<BodyHeight> {

    @Override
    public Optional<DataPoint<BodyHeight>> asDataPoint(JsonNode listNode) {

        JsonNode valueListNode = asRequiredNode(listNode, getValueListNodeName());
        double bodyHeightValue = asRequiredDouble(valueListNode.get(0), "fpVal");
        if (bodyHeightValue == 0) {
            return Optional.empty();
        }
        BodyHeight.Builder bodyHeightBuilder =
                new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER, bodyHeightValue));

        setEffectiveTimeFrameIfPresent(bodyHeightBuilder, listNode);

        BodyHeight bodyHeight = bodyHeightBuilder.build();
        Optional<String> originDataSourceId = asOptionalString(listNode, "originDataSourceId");
        return Optional.of(newDataPoint(bodyHeight, originDataSourceId.orElse(null)));
    }


}
