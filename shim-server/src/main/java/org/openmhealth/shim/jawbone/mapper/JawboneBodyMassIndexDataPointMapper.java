package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyMassIndex;
import org.openmhealth.schema.domain.omh.BodyMassIndexUnit;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.TypedUnitValue;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;
import static org.openmhealth.shim.jawbone.mapper.JawboneBodyEventType.BODY_MASS_INDEX;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/body">API documentation</a>
 */
public class JawboneBodyMassIndexDataPointMapper extends JawboneBodyEventsDataPointMapper<BodyMassIndex>{

    @Override
    Optional<Measure.Builder<BodyMassIndex, ?>> newMeasureBuilder(JsonNode listEntryNode) {

        TypedUnitValue<BodyMassIndexUnit> bmiValue =
                new TypedUnitValue<>(KILOGRAMS_PER_SQUARE_METER, asRequiredDouble(listEntryNode, "bmi"));

        return Optional.of(new BodyMassIndex.Builder(bmiValue));
    }

    @Override
    protected JawboneBodyEventType getBodyEventType() {
        return BODY_MASS_INDEX;
    }
}
