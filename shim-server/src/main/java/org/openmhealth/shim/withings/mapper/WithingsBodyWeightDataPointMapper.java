package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.schema.domain.omh.Measure;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.BODY_WEIGHT;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyWeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyWeight> {

    @Override
    public Optional<Measure.Builder<BodyWeight, ?>> newMeasureBuilder(JsonNode measuresNode) {

        Optional<BigDecimal> value = getValueForMeasureType(measuresNode, BODY_WEIGHT);

        if (!value.isPresent()) {
            return empty();
        }

        return Optional.of(new BodyWeight.Builder(new MassUnitValue(KILOGRAM, value.get())));
    }
}
