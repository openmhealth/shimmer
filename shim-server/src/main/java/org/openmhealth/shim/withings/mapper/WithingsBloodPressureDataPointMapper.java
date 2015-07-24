package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureType
        .DIASTOLIC_BLOOD_PRESSURE;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureType
        .SYSTOLIC_BLOOD_PRESSURE;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link BloodPressure} objects
 * when both systolic and
 * diastolic values are present in the response
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBloodPressureDataPointMapper extends WithingsBodyMeasureDataPointMapper<BloodPressure> {

    /**
     * @param node list node from the array "measuregrp" contained in the "body" of the endpoint response
     * @param timeZoneFullName a string containing the full name of the time zone (e.g., America/Los_Angeles) from the
     * "timezone" property of the "body" of the body measure endpoint response
     * @return a {@link DataPoint} object containing a {@link BloodPressure} measure with the appropriate values from
     * the JSON node parameter, wrapped as an {@link Optional}
     */
    @Override
    public Optional<DataPoint<BloodPressure>> asDataPoint(JsonNode node, String timeZoneFullName) {

        JsonNode measuresNode = asRequiredNode(node, "measures");

        if (isGoal(node)) {
            return Optional.empty();
        }

        Double diastolicValue = null, systolicValue = null;
        Long diastolicUnit = null, systolicUnit = null;

        for (JsonNode measureNode : measuresNode) {
            // We assume that there is only one value and unit for each measure type in the measures array
            // This implementation, in essence, grabs the value and unit for the last measure of that type in the list
            Long type = asRequiredLong(measureNode, "type");
            if (type == DIASTOLIC_BLOOD_PRESSURE.getMagicNumber()) {
                diastolicValue = asRequiredDouble(measureNode, "value");
                diastolicUnit = asRequiredLong(measureNode, "unit");
            }
            else if (type == SYSTOLIC_BLOOD_PRESSURE.getMagicNumber()) {
                systolicValue = asRequiredDouble(measureNode, "value");
                systolicUnit = asRequiredLong(measureNode, "unit");
            }
        }

        if (diastolicValue == null || diastolicUnit == null || systolicValue == null || systolicUnit == null) {
            // We are missing a unit or value from one of the bp measurements and therefore unable to create a datapoint
            if (diastolicValue != null || diastolicUnit != null || systolicValue != null || systolicUnit != null) {
                //TODO: log or record that we were unable to map data point because we only had some of the values
                // In this case, there was a value or unit for at least one of these, however there was not complete
                // information so we are unable to map the datapoint and we should let them know. If we skip this
                // step, it implies that there was not any blood pressure related information in the measrgrp which
                // is one expected outcome, so we do not need to document that situation
            }
            return Optional.empty();
        }

        SystolicBloodPressure systolicBloodPressure = new SystolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY,
                actualValueOf(systolicValue, systolicUnit));
        DiastolicBloodPressure diastolicBloodPressure = new DiastolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY,
                actualValueOf(diastolicValue, diastolicUnit));
        BloodPressure.Builder bloodPressureBuilder =
                new BloodPressure.Builder(systolicBloodPressure, diastolicBloodPressure);

        Optional<Long> dateInEpochSeconds = asOptionalLong(node, "date");
        if (dateInEpochSeconds.isPresent()) {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateInEpochSeconds.get()),
                    ZoneId.of(timeZoneFullName));
            bloodPressureBuilder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            bloodPressureBuilder.setUserNotes(userComment.get());
        }

        BloodPressure bloodPressureMeasure = bloodPressureBuilder.build();
        Optional<Long> externalId = asOptionalLong(node, "grpid");
        DataPoint<BloodPressure> bloodPressureDataPoint =
                newDataPoint(bloodPressureMeasure, externalId.orElse(null),
                        isSensed(node).orElse(null), null);
        return Optional.of(bloodPressureDataPoint);

    }

}
