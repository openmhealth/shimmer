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
 *
 * @author Chris Schaefbauer
 */
public class FitbitBodyWeightDataPointMapper extends FitbitDataPointMapper<BodyWeight> {

    /**
     * Maps a JSON response node from the Fitbit API into a {@link BodyWeight} measure
     *
     * @param node a JSON node for an individual object in the "weight" array retrieved from the body/log/weight Fitbit
     * API call
     * @return a {@link DataPoint} object containing a {@link BodyWeight} measure with the appropriate values from the
     * JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node) {

        MassUnitValue bodyWeight = new MassUnitValue(MassUnit.KILOGRAM, asRequiredDouble(node, "weight"));
        BodyWeight.Builder builder = new BodyWeight.Builder(bodyWeight);

        Optional<OffsetDateTime> dateTime = combineDateTimeAndTimezone(node);

        if (dateTime.isPresent()) {
            builder.setEffectiveTimeFrame(dateTime.get());
        }


        Optional<Long> externalId = asOptionalLong(node, "logId");
        BodyWeight measure = builder.build();

        return Optional.of(newDataPoint(measure, externalId.orElse(null)));

    }

    /**
     * @return the name of the list node returned from Fitbit Resource API body/log/weight response
     */
    @Override
    protected String getListNodeName() {
        return "weight";
    }
}
