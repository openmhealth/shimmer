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
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsHeartRateDataPointMapper extends WithingsBodyMeasureDataPointMapper<HeartRate> {

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

        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(trueValueOf(value, unit));

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

    /**
     * DO NOT USE
     */
    @Override
    Optional<DataPoint<HeartRate>> asDataPoint(JsonNode node) {
        return null;
    }
}
