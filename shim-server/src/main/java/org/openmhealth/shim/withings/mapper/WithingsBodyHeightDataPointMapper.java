package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.schema.domain.omh.Measure;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.BODY_HEIGHT;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsBodyHeightDataPointMapper extends WithingsBodyMeasureDataPointMapper<BodyHeight> {

    @Override
    public Optional<Measure.Builder<BodyHeight, ?>> newMeasureBuilder(JsonNode measuresNode) {

        Optional<BigDecimal> value = getValueForType(measuresNode, BODY_HEIGHT);

        if (!value.isPresent()) {
            return empty();
        }

        return Optional.of(new BodyHeight.Builder(new LengthUnitValue(METER, value.get())));
    }
}
