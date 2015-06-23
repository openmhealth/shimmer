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
 * A mapper from Fitbit Resource API body/log/weight responses to {@link BodyWeight} objects
 * @author Chris Schaefbauer
 */
public class FitbitBodyWeightDataPointMapper extends FitbitDataPointMapper<BodyWeight>{

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node, int UTCOffsetInMilliseconds) {
        MassUnitValue bodyWeight = new MassUnitValue(MassUnit.KILOGRAM,asRequiredDouble(node,"weight"));
        BodyWeight.Builder builder = new BodyWeight.Builder(bodyWeight);

        Optional<OffsetDateTime> dateTime = combineDateTimeAndTimezone(node,UTCOffsetInMilliseconds);

        if(dateTime.isPresent()){
            builder.setEffectiveTimeFrame(dateTime.get());
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
