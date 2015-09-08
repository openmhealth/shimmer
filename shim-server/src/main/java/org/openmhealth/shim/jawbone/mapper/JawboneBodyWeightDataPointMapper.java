package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.jawbone.mapper.JawboneBodyEventType.BODY_WEIGHT;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/body">API documentation</a>
 */
public class JawboneBodyWeightDataPointMapper extends JawboneBodyEventsDataPointMapper<BodyWeight> {

    @Override
    protected Optional<Measure.Builder<BodyWeight, ?>> newMeasureBuilder(JsonNode listEntryNode) {

        Optional<Double> optionalWeight = asOptionalDouble(listEntryNode, "weight");

        if (!optionalWeight.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new BodyWeight.Builder(new MassUnitValue(KILOGRAM, optionalWeight.get())));
    }

    @Override
    protected JawboneBodyEventType getBodyEventType() {
        return BODY_WEIGHT;
    }
}
