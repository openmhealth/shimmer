package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureTypes.*;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link HeartRate} objects when a
 * heart rate value is present in the body measure group
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsHeartRateDataPointMapper extends WithingsBodyMeasureDataPointMapper<HeartRate> {

    /**
     * Maps a JSON response node from the Withings body measure endpoint into a {@link HeartRate} measure
     *
     * @param node list node from the array "measuregrp" contained in the "body" of the endpoint response
     * @param timeZoneFullName a string containing the full name of the time zone (e.g., America/Los_Angeles) from the
     * "timezone" property of the "body" of the body measure endpoint response
     * @return a {@link DataPoint} object containing a {@link HeartRate} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    Optional<DataPoint<HeartRate>> asDataPoint(JsonNode node, String timeZoneFullName) {
        JsonNode measuresNode = asRequiredNode(node, "measures");

        Double value = null;
        Long unit = null;

        for (JsonNode measureNode : measuresNode) {
            Long type = asRequiredLong(measureNode, "type");
            if (type == HEART_PULSE.getIntVal()) {
                value = asRequiredDouble(measureNode, "value");
                unit = asRequiredLong(measureNode, "unit");
            }
        }

        if (value == null || unit == null) {
            //this measuregrp does not have a heart rate measure
            return Optional.empty();
        }

        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(actualValueOf(value, unit));

        Optional<Long> dateInEpochSecs = asOptionalLong(node, "date");
        if (dateInEpochSecs.isPresent()) {
            OffsetDateTime offsetDateTime =
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateInEpochSecs.get()), ZoneId.of(timeZoneFullName));
            heartRateBuilder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            heartRateBuilder.setUserNotes(userComment.get());
        }

        HeartRate heartRate = heartRateBuilder.build();
        Optional<Long> externalId = asOptionalLong(node, "grpid");
        DataPoint<HeartRate> heartRateDataPoint =
                newDataPoint(heartRate, RESOURCE_API_SOURCE_NAME, externalId.orElse(null), isSensed(node).orElse(null));
        return Optional.of(heartRateDataPoint);
    }

}
