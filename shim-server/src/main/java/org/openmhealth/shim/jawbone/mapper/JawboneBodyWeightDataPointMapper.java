package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;


/**
 * @author Chris Schaefbauer
 */
public class JawboneBodyWeightDataPointMapper extends JawboneBodyEventsDataPointMapper<BodyWeight> {

    @Override
    protected Optional<Measure.Builder<BodyWeight, ?>> newMeasureBuilder(JsonNode listEntryNode) {

        Optional<Double> optionalWeight = asOptionalDouble(listEntryNode, "weight");
        if(optionalWeight.isPresent()){
            if(optionalWeight.get() == null){
                return Optional.empty();
            }
            BodyWeight.Builder bodyWeightBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,optionalWeight.get()));
            return Optional.of(bodyWeightBuilder);
        }

        return Optional.empty();
    }

    @Override
    protected JawboneBodyEventType getBodyEventType() {
        return JawboneBodyEventType.BODY_WEIGHT;
    }


}
