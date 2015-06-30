package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.lang.Math.pow;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureTypes.*;


/**
 * Created by Chris Schaefbauer on 6/29/15.
 */
public class WithingsBodyWeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyWeight> {

    @Override
    Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node, String timeZoneFullName) {
        JsonNode measuresNode = asRequiredNode(node, "measures");
        Double value = null;
        Long unit = null;
        for(JsonNode measureNode : measuresNode){
            if(asRequiredLong(measureNode,"type")== WEIGHT.getIntVal()){
                value = asRequiredDouble(measureNode,"value");
                unit = asRequiredLong(measureNode,"unit");
            }
        }

        if(value == null || unit ==null){
            return Optional.empty(); // there was no body weight measure in this node
        }

        BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,(value*pow(10, unit))));
        
        Optional<Long> dateTimeInUtcSec = asOptionalLong(node, "date");
        if(dateTimeInUtcSec.isPresent()){
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateTimeInUtcSec.get()),
                    ZoneId.of(timeZoneFullName));

            bodyWeightBuilder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<Long> externalId = asOptionalLong(node, "grpid");

        Optional<Long> measurementProcess = asOptionalLong(node, "attrib");

        Boolean sensed=null;
        if(measurementProcess.isPresent()){
            if (measurementProcess.get()==0 || measurementProcess.get()==1){ //TODO: Need to check the semantics of 1
                sensed = true;
            }
            else{
                sensed = false;
            }
        }

        BodyWeight bodyWeight = bodyWeightBuilder.build();

        return Optional.of(newDataPoint(bodyWeight,RESOURCE_API_SOURCE_NAME,externalId.orElse(null),sensed));
    }


    /**
     * Not to be used, required to be implemented from WithingsDataPointMapper superclass
     */
    @Override
    Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node) {
        return null;
    }
}
