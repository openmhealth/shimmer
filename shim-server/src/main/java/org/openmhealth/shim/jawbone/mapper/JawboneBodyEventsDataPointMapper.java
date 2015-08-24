package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * @author Chris Schaefbauer
 */
public abstract class JawboneBodyEventsDataPointMapper<T extends Measure> extends JawboneDataPointMapper<T> {

    @Override
    protected Optional<T> getMeasure(JsonNode listEntryNode) {

        if(!containsType(listEntryNode,getBodyEventType())){
            return Optional.empty();
        }

        Optional<Measure.Builder<T, ?>> builderOptional = newMeasureBuilder(listEntryNode);
        if(!builderOptional.isPresent()){
            return Optional.empty();
        }

        Measure.Builder<T,?> builder = builderOptional.get();

        setEffectiveTimeFrame(builder,listEntryNode);

        Optional<String> optionalUserNote = asOptionalString(listEntryNode, "note");
        optionalUserNote.ifPresent(userNote->builder.setUserNotes(userNote));

        return Optional.of(builder.build());

    }

    private boolean containsType(JsonNode listEntryNode,JawboneBodyEventType bodyEventType) {

        if(bodyEventType == JawboneBodyEventType.BODY_WEIGHT || bodyEventType == JawboneBodyEventType.BODY_MASS_INDEX){
            Optional<Double> optionalPropertyValue = asOptionalDouble(listEntryNode, bodyEventType.getPropertyName());
            if(optionalPropertyValue.isPresent()){
                if(optionalPropertyValue.get()!=null){
                    return true;
                }
            }
        }
        return false;
    }

    abstract Optional<Measure.Builder<T, ?>> newMeasureBuilder(JsonNode measuresNode);

    abstract protected JawboneBodyEventType getBodyEventType();

}
