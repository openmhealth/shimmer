package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureTypes.HEIGHT;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link BodyHeight} objects when a
 * body height value is present in the body measure group
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyHeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyHeight> {

    /**
     * Maps a JSON response node from the Withings body measure endpoint into a {@link BodyHeight} measure
     *
     * @param node list node from the array "measuregrp" contained in the "body" of the endpoint response
     * @param timeZoneFullName a string containing the full name of the time zone (e.g., America/Los_Angeles) from the
     * "timezone" property of the "body" of the body measure endpoint response
     * @return a {@link DataPoint} object containing a {@link BodyHeight} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    Optional<DataPoint<BodyHeight>> asDataPoint(JsonNode node, String timeZoneFullName) {
        JsonNode measuresNode = asRequiredNode(node, "measures");
        Double value = null;
        Long unit = null;
        for (JsonNode measureNode : measuresNode) {
            //within each measure group, we look through all the measures to find the height value in that group
            Long type = asRequiredLong(measureNode, "type");
            if (type == HEIGHT.getIntVal()) {
                value = asRequiredDouble(measureNode, "value");
                unit = asRequiredLong(measureNode, "unit");
            }
        }

        if (value == null || unit == null) {
            //There is no height data point in this measure group, so we return an empty optional value
            return Optional.empty();
        }

        BodyHeight.Builder builder = new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER,
                actualValueOf(value, unit)));

        Optional<Long> dateInEpochSec = asOptionalLong(node, "date");
        if (dateInEpochSec.isPresent()) {

            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateInEpochSec.get()),
                    ZoneId.of(timeZoneFullName));
            builder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            builder.setUserNotes(userComment.get());
        }

        BodyHeight measure = builder.build();
        Optional<Long> groupId = asOptionalLong(node, "grpid");
        DataPoint<BodyHeight> bodyHeightDataPoint =
                newDataPoint(measure, RESOURCE_API_SOURCE_NAME, groupId.orElse(null), isSensed(node).orElse(null));

        return Optional.of(bodyHeightDataPoint);
    }


}
