package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.time.ZoneId.of;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureType.WEIGHT;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link BodyWeight} objects when
 * a
 * body weight value is present in the body measure group
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyWeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyWeight> {

    @Override
    public Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node) {

        JsonNode measuresNode = asRequiredNode(node, "measures");
        if (isGoal(node)) {
            return Optional.empty();
        }
        Double value = null;
        Long unit = null;
        for (JsonNode measureNode : measuresNode) {
            if (asRequiredLong(measureNode, "type") == WEIGHT.getMagicNumber()) {
                value = asRequiredDouble(measureNode, "value");
                unit = asRequiredLong(measureNode, "unit");
            }
        }

        if (value == null || unit == null) {
            return Optional.empty();
        }

        if (isUnattributedSensed(node)) {
            //This is a corner case captured by the Withings API where the data point value captured by the scale is
            // similar to multiple users and they were not prompted to specify the data point owner because the new
            // user was created and not synced to the scale before taking a measurement
            //TODO: Log that datapoint was not captured and to be revisited since user can assign in the web interface
            return Optional.empty();
        }

        BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,
                actualValueOf(value, unit)));

        Optional<Long> dateTimeInUtcSec = asOptionalLong(node, "date");
        if (dateTimeInUtcSec.isPresent()) {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateTimeInUtcSec.get()),
                    of("Z"));

            bodyWeightBuilder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            bodyWeightBuilder.setUserNotes(userComment.get());
        }

        Optional<Long> externalId = asOptionalLong(node, "grpid");

        BodyWeight bodyWeight = bodyWeightBuilder.build();

        return Optional.of(newDataPoint(bodyWeight, externalId.orElse(null),
                isSensed(node).orElse(null), null));

    }


}
