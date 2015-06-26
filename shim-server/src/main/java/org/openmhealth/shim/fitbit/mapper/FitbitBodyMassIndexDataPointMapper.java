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
 *
 * @author Chris Schaefbauer
 */
public class FitbitBodyMassIndexDataPointMapper extends FitbitDataPointMapper<BodyMassIndex> {

    /**
     * Maps a JSON response node from the Fitbit API into a {@link BodyMassIndex} measure
     *
     * @param node a JSON node for an individual object in the "weight" array retrieved from the body/log/weight Fitbit
     * API call
     * @param offsetFromUTCInMilliseconds the "offsetFromUTCMillis" property from a JSON response node from the
     * user/<user-id>/profile Fitbit API call, may be incorrect if the user has changed time zone since the data point
     * was created
     * @return a {@link DataPoint} object containing a {@link BodyMassIndex} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    protected Optional<DataPoint<BodyMassIndex>> asDataPoint(JsonNode node, int offsetFromUTCInMilliseconds) {
        TypedUnitValue<BodyMassIndexUnit> bmiValue =
                new TypedUnitValue<BodyMassIndexUnit>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER,
                        asRequiredDouble(node, "bmi"));
        BodyMassIndex.Builder builder = new BodyMassIndex.Builder(bmiValue);

        Optional<OffsetDateTime> dateTime = combineDateTimeAndTimezone(node, offsetFromUTCInMilliseconds);

        if (dateTime.isPresent()) {
            builder.setEffectiveTimeFrame(dateTime.get());
        }

        Optional<Long> externalId = asOptionalLong(node, "logId");
        return Optional.of(newDataPoint(builder.build(), externalId.orElse(null)));
    }

    /**
     * @return the name of the list node returned from Fitbit Resource API body/log/weight response
     */
    @Override
    protected String getListNodeName() {
        return "weight";
    }
}
