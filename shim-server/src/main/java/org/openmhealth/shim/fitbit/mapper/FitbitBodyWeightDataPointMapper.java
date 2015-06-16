package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;

import java.time.*;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;

/**
 * Created by Chris Schaefbauer on 6/10/15.
 */
public class FitbitBodyWeightDataPointMapper extends FitbitDataPointMapper<BodyWeight>{

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node, int UTCOffsetInMilliseconds) {
        MassUnitValue bodyWeight = new MassUnitValue(MassUnit.KILOGRAM,asRequiredDouble(node,"weight"));
        BodyWeight.Builder builder = new BodyWeight.Builder(bodyWeight);

        Optional<LocalDateTime> dateTime = asOptionalLocalDateTime(node,"date","time");

        if(dateTime.isPresent()){
            OffsetDateTime offsetDateTime = OffsetDateTime.of(dateTime.get(), ZoneOffset.ofTotalSeconds(UTCOffsetInMilliseconds / 1000));
            builder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<Long> externalId = asOptionalLong(node, "logId");
        BodyWeight measure = builder.build();

        return Optional.of(newDataPoint(measure, externalId.orElse(null)));

    }

    @Override
    protected String getListNodeName() {
        return "weight";
    }
}
