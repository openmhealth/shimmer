package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * Created by Chris Schaefbauer on 7/13/15.
 */
public class GoogleFitHeartRateDataPointMapper extends GoogleFitDataPointMapper<HeartRate>{

    @Override
    protected Optional<DataPoint<HeartRate>> asDataPoint(JsonNode listNode) {
        JsonNode valueListNode = asRequiredNode(listNode,"value");
        double heartRateValue = asRequiredDouble(valueListNode.get(0),"fpVal");
        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(heartRateValue);
        setEffectiveTimeFrameIfPresent(heartRateBuilder,listNode);
        HeartRate heartRate = heartRateBuilder.build();
        Optional<String> originDataSourceId = asOptionalString(listNode,"originDataSourceId");
        return Optional.of(newDataPoint(heartRate,originDataSourceId.orElse(null)));
    }
}
