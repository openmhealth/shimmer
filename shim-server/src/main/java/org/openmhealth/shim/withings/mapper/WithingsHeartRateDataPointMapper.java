package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.openmhealth.schema.domain.omh.Measure;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.HEART_RATE;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsHeartRateDataPointMapper extends WithingsBodyMeasureDataPointMapper<HeartRate> {

    @Override
    public Optional<Measure.Builder<HeartRate, ?>> newMeasureBuilder(JsonNode measuresNode) {

        Optional<BigDecimal> value = getValueForMeasureType(measuresNode, HEART_RATE);

        if (!value.isPresent()) {
            return empty();
        }

        return Optional.of(new HeartRate.Builder(value.get()));
    }
}
