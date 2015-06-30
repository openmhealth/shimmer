package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.LengthUnit;
import org.openmhealth.schema.domain.omh.LengthUnitValue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureTypes.HEIGHT;


/**
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsBodyHeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyHeight>{

    @Override
    Optional<DataPoint<BodyHeight>> asDataPoint(JsonNode node, String timeZoneFullName) {
        JsonNode measuresNode = asRequiredNode(node, "measures");
        Double value = null;
        Long unit = null;
        for(JsonNode measureNode : measuresNode){
            //within each measure group, we look through all the measures to find the height value in that group
            Long type = asRequiredLong(measureNode, "type");
            if(type== HEIGHT.getIntVal()){
                value = asRequiredDouble(measureNode,"value");
                unit = asRequiredLong(measureNode,"unit");
            }
        }

        if(value == null || unit == null){
            //There is no height data point in this measure group, so we return an empty optional value
            return Optional.empty();
        }

        BodyHeight.Builder builder = new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER,trueValueOf(value,unit)));

        Optional<Long> dateInEpochSec = asOptionalLong(node, "date");
        if(dateInEpochSec.isPresent()){

            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateInEpochSec.get()),
                    ZoneId.of(timeZoneFullName));
            builder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if(userComment.isPresent()){
            builder.setUserNotes(userComment.get());
        }

        BodyHeight measure = builder.build();
        Optional<Long> groupId = asOptionalLong(node,"grpid");
        DataPoint<BodyHeight> bodyHeightDataPoint =
                newDataPoint(measure, RESOURCE_API_SOURCE_NAME, groupId.orElse(null), isSensed(node).orElse(null));

        return Optional.of(bodyHeightDataPoint);
    }

    /**
     * DO NOT USE THIS METHOD
     *
     */
    @Override
    Optional<DataPoint<BodyHeight>> asDataPoint(JsonNode node) {
        return null;
    }
}
