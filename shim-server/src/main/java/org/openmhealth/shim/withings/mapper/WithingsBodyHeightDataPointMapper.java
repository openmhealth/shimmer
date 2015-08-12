package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.LengthUnit;
import org.openmhealth.schema.domain.omh.LengthUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureType.HEIGHT;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link BodyHeight} objects when
 * a
 * body height value is present in the body measure group.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyHeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyHeight> {

    @Override
    public Optional<DataPoint<BodyHeight>> asDataPoint(JsonNode listEntryNode) {

        JsonNode measuresNode = asRequiredNode(listEntryNode, "measures");

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

        setEffectiveTimeFrame(builder, listEntryNode);
        setUserComment(builder, listEntryNode);

        BodyHeight measure = builder.build();
        Optional<Long> groupId = asOptionalLong(listEntryNode, "grpid");
        DataPoint<BodyHeight> bodyHeightDataPoint =
                newDataPoint(measure, groupId.orElse(null), isSensed(listEntryNode).orElse(null),
                        null);

        return Optional.of(bodyHeightDataPoint);

    }


}
