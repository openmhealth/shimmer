package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.Measure.*;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/body">API documentation</a>
 */
public abstract class JawboneBodyEventsDataPointMapper<T extends Measure> extends JawboneDataPointMapper<T> {

    /**
     * Handles some of the basic measure creation activities for body event datapoints and then delegates the creation
     * of the {@link Builder} to subclasses for each individual body event {@link Measure}.
     *
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     */
    @Override
    protected Optional<T> getMeasure(JsonNode listEntryNode) {

        if (!containsType(listEntryNode, getBodyEventType())) {
            return Optional.empty();
        }

        Optional<Measure.Builder<T, ?>> builderOptional = newMeasureBuilder(listEntryNode);
        if (!builderOptional.isPresent()) {
            return Optional.empty();
        }

        Measure.Builder<T, ?> builder = builderOptional.get();

        setEffectiveTimeFrame(builder, listEntryNode);

        Optional<String> optionalUserNote = asOptionalString(listEntryNode, "note");
        optionalUserNote.ifPresent(userNote -> builder.setUserNotes(userNote));

        return Optional.of(builder.build());

    }

    /**
     * Determines whether a list entry node contains a body event of a certain type.
     *
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     * @param bodyEventType the target body event type
     */
    private boolean containsType(JsonNode listEntryNode, JawboneBodyEventType bodyEventType) {

        if (bodyEventType == JawboneBodyEventType.BODY_WEIGHT ||
                bodyEventType == JawboneBodyEventType.BODY_MASS_INDEX) {
            Optional<Double> optionalPropertyValue = asOptionalDouble(listEntryNode, bodyEventType.getPropertyName());
            if (optionalPropertyValue.isPresent()) {
                if (optionalPropertyValue.get() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a {@link Builder} for a specific body event measure
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     */
    abstract Optional<Measure.Builder<T, ?>> newMeasureBuilder(JsonNode listEntryNode);

    abstract protected JawboneBodyEventType getBodyEventType();

}
