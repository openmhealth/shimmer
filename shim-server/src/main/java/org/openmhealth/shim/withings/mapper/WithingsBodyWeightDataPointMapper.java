package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureType.WEIGHT;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link BodyWeight} objects when
 * a
 * body weight value is present in the body measure group.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyWeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyWeight> {

    @Override
    public Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode node) {

        JsonNode measuresNode = asRequiredNode(node, "measures");

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

        BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,
                actualValueOf(value, unit)));

        setEffectiveTimeFrame(bodyWeightBuilder, node);
        setUserComment(bodyWeightBuilder, node);

        Optional<Long> externalId = asOptionalLong(node, "grpid");

        BodyWeight bodyWeight = bodyWeightBuilder.build();

        return Optional.of(newDataPoint(bodyWeight, externalId.orElse(null),
                isSensed(node).orElse(null), null));

    }


}
