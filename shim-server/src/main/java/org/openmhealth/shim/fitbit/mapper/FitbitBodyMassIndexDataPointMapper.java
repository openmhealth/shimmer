package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyMassIndex;
import org.openmhealth.schema.domain.omh.BodyMassIndexUnit;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.TypedUnitValue;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLong;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;

/**
 * A mapper from Fitbit Resource API body/log/weight responses to {@link BodyMassIndex} objects
 * @author Chris Schaefbauer
 */
public class FitbitBodyMassIndexDataPointMapper extends FitbitDataPointMapper<BodyMassIndex>{

    @Override
    protected Optional<DataPoint<BodyMassIndex>> asDataPoint(JsonNode node, int UTCOffsetInMilliseconds) {
        TypedUnitValue<BodyMassIndexUnit> bmiValue = new TypedUnitValue<BodyMassIndexUnit>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER,asRequiredDouble(node,"bmi"));
        BodyMassIndex.Builder builder = new BodyMassIndex.Builder( bmiValue);

        Optional<OffsetDateTime> dateTime = combineDateTimeAndTimezone(node,UTCOffsetInMilliseconds);
        //asOptionalLocalDateTime(node,"date","time");

        if(dateTime.isPresent()){
//            OffsetDateTime offsetDateTime = OffsetDateTime.of(dateTime.get(), ZoneOffset.ofTotalSeconds(UTCOffsetInMilliseconds / 1000));
            builder.setEffectiveTimeFrame(dateTime.get());
        }


        Optional<Long> externalId = asOptionalLong(node,"logId");
        return Optional.of(newDataPoint(builder.build(), externalId.orElse(null)));
    }

    @Override
    protected String getListNodeName() {
        return "weight";
    }
}
