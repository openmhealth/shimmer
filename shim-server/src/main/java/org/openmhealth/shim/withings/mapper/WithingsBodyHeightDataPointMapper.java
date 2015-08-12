package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.LengthUnit;
import org.openmhealth.schema.domain.omh.LengthUnitValue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.time.ZoneId.of;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureType.HEIGHT;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link BodyHeight} objects when
 * a
 * body height value is present in the body measure group
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyHeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyHeight> {

    @Override
    public Optional<DataPoint<BodyHeight>> asDataPoint(JsonNode node) {

        JsonNode measuresNode = asRequiredNode(node, "measures");

        // We only map measurements, not goals in the Withings API
        if (isGoal(node)) {
            return Optional.empty();
        }

        Double value = null;
        Long unit = null;

        for (JsonNode measureNode : measuresNode) {
            Long type = asRequiredLong(measureNode, "type");
            if (type == HEIGHT.getMagicNumber()) {
                value = asRequiredDouble(measureNode, "value");
                unit = asRequiredLong(measureNode, "unit");
            }
        }

        if (value == null || unit == null) {
            // There is no height data point in this measure group, so we return an empty optional value
            return Optional.empty();
        }

        BodyHeight.Builder builder = new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER,
                actualValueOf(value, unit)));

        Optional<Long> dateInEpochSec = asOptionalLong(node, "date");
        if (dateInEpochSec.isPresent()) {

            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateInEpochSec.get()),
                    of("Z"));
            builder.setEffectiveTimeFrame(offsetDateTime);
        }

        Optional<String> userComment = asOptionalString(node, "comment");
        if (userComment.isPresent()) {
            builder.setUserNotes(userComment.get());
        }

        BodyHeight measure = builder.build();
        Optional<Long> groupId = asOptionalLong(node, "grpid");
        DataPoint<BodyHeight> bodyHeightDataPoint =
                newDataPoint(measure, groupId.orElse(null), isSensed(node).orElse(null),
                        null);

        return Optional.of(bodyHeightDataPoint);

    }


}
