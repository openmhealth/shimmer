package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.LengthUnit;
import org.openmhealth.schema.domain.omh.LengthUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * Created by Chris Schaefbauer on 7/13/15.
 */
public class GoogleFitBodyHeightDataPointMapper extends GoogleFitDataPointMapper<BodyHeight>{

    @Override
    public Optional<DataPoint<BodyHeight>> asDataPoint(JsonNode listNode) {

        JsonNode valueListNode = asRequiredNode(listNode,getValueListNodeName());
        double bodyHeightValue = asRequiredDouble(valueListNode.get(0),"fpVal");
        BodyHeight.Builder bodyHeightBuilder = new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER,bodyHeightValue));

        setEffectiveTimeFrameIfPresent(bodyHeightBuilder, listNode);

        BodyHeight bodyHeight = bodyHeightBuilder.build();
        return Optional.of(newDataPoint(bodyHeight, null));
    }




}
