package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyMassIndex;
import org.openmhealth.schema.domain.omh.BodyMassIndexUnit;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.TypedUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * @author Chris Schaefbauer
 */
public class JawboneBodyMassIndexDataPointMapper extends JawboneBodyEventsDataPointMapper<BodyMassIndex>{

    @Override
    Optional<Measure.Builder<BodyMassIndex, ?>> newMeasureBuilder(JsonNode measuresNode) {
        TypedUnitValue<BodyMassIndexUnit> bmiValue =
                new TypedUnitValue<>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER,
                        asRequiredDouble(measuresNode, "bmi"));
        Measure.Builder<BodyMassIndex, ?> bmiBuilder = new BodyMassIndex.Builder(bmiValue);
        return Optional.of(bmiBuilder);
    }

    @Override
    protected JawboneBodyEventType getBodyEventType() {
        return JawboneBodyEventType.BODY_MASS_INDEX;
    }
}
