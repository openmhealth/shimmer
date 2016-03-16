/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.Measure.Builder;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.jawbone.mapper.JawboneBodyEventType.BODY_MASS_INDEX;
import static org.openmhealth.shim.jawbone.mapper.JawboneBodyEventType.BODY_WEIGHT;


/**
 * Base class for Jawbone mappers that translate different individual body events (e.g., weight, BMI) into {@link
 * Measure} objects.
 *
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
        optionalUserNote.ifPresent(builder::setUserNotes);

        return Optional.of(builder.build());
    }

    /**
     * Determines whether a list entry node contains a body event of a certain type.
     *
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     * @param bodyEventType the target body event type
     */
    private boolean containsType(JsonNode listEntryNode, JawboneBodyEventType bodyEventType) {

        if (bodyEventType == BODY_WEIGHT || bodyEventType == BODY_MASS_INDEX) {
            if (asOptionalDouble(listEntryNode, bodyEventType.getPropertyName()).isPresent()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a {@link Builder} for a specific body event measure.
     *
     * @param listEntryNode an individual entry node from the "items" array of a Jawbone endpoint response
     */
    abstract Optional<Measure.Builder<T, ?>> newMeasureBuilder(JsonNode listEntryNode);

    // TODO add Javadoc
    abstract protected JawboneBodyEventType getBodyEventType();
}
