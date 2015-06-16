package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyMassIndex;
import org.openmhealth.schema.domain.omh.BodyMassIndexUnit;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.TypedUnitValue;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLocalDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;

/**
 * Created by Chris Schaefbauer on 6/15/15.
 */
public class FitbitBodyMassIndexDataPointMapper extends FitbitDataPointMapper<BodyMassIndex>{

    @Override
    protected Optional<DataPoint<BodyMassIndex>> asDataPoint(JsonNode node, int UTCOffsetInMilliseconds) {
        TypedUnitValue<BodyMassIndexUnit> bmiValue = new TypedUnitValue<BodyMassIndexUnit>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER,asRequiredDouble(node,"bmi"));
        BodyMassIndex.Builder builder = new BodyMassIndex.Builder( bmiValue);

        Optional<LocalDateTime> dateTime = asOptionalLocalDateTime(node,"date","time");

        if(dateTime.isPresent()){
            OffsetDateTime offsetDateTime = OffsetDateTime.of(dateTime.get(), ZoneOffset.ofTotalSeconds(UTCOffsetInMilliseconds / 1000));
            builder.setEffectiveTimeFrame(offsetDateTime);
        }


        Optional<Long> externalId = asOptionalLong(node,"logId");
        return Optional.of(newDataPoint(builder.build(), externalId.orElse(null)));
    }

    @Override
    protected String getListNodeName() {
        return "weight";
    }
}
