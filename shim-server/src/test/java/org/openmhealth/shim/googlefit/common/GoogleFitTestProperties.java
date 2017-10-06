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

package org.openmhealth.shim.googlefit.common;

import org.openmhealth.schema.domain.omh.DataPointModality;
import org.openmhealth.schema.domain.omh.SchemaId;
import org.openmhealth.schema.domain.omh.TimeFrame;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class GoogleFitTestProperties {

    private String sourceOriginId;
    private SchemaId bodySchemaId;
    private DataPointModality modality;
    private List<Object> values = new ArrayList<>();
    private OffsetDateTime effectiveStartDateTime;
    private OffsetDateTime effectiveEndDateTime;

    public String getSourceOriginId() {

        return sourceOriginId;
    }

    public void setSourceOriginId(String sourceOriginId) {
        this.sourceOriginId = sourceOriginId;
    }

    public SchemaId getBodySchemaId() {
        return bodySchemaId;
    }

    public void setBodySchemaId(SchemaId bodySchemaId) {
        this.bodySchemaId = bodySchemaId;
    }

    public Optional<DataPointModality> getModality() {

        return Optional.ofNullable(modality);
    }

    public void setModality(DataPointModality modality) {
        this.modality = modality;
    }

    public String getStringValue() {
        return getStringValue(0);
    }

    public String getStringValue(int index) {
        return getValue(String.class, index);
    }

    public <T> T getValue(Class<T> clazz, int index) {
        return clazz.cast(values.get(index));
    }

    public Long getIntValue() {
        return getIntValue(0);
    }

    public Long getIntValue(int index) {
        return getValue(Long.class, index);
    }

    public Double getFpValue() {
        return getFpValue(0);
    }

    public Double getFpValue(int index) {
        return getValue(Double.class, index);
    }

    public GoogleFitTestProperties addValue(Object value) {
        this.values.add(value);
        return this;
    }

    public Optional<OffsetDateTime> getEffectiveEndDateTime() {

        return Optional.ofNullable(effectiveEndDateTime);
    }

    public void setEffectiveEndDateTime(String effectiveEndDateTime) {
        this.effectiveEndDateTime = OffsetDateTime.parse(effectiveEndDateTime);
    }

    public Optional<OffsetDateTime> getEffectiveStartDateTime() {

        return Optional.ofNullable(effectiveStartDateTime);
    }

    public void setEffectiveStartDateTime(String effectiveStartDateTime) {
        this.effectiveStartDateTime = OffsetDateTime.parse(effectiveStartDateTime);
    }

    public Optional<TimeFrame> getEffectiveTimeFrame() {

        if (getEffectiveStartDateTime().isPresent() && getEffectiveEndDateTime().isPresent()) {
            return Optional.of(new TimeFrame(ofStartDateTimeAndEndDateTime(
                    getEffectiveStartDateTime().get(),
                    getEffectiveEndDateTime().get())));
        }
        else if (getEffectiveStartDateTime().isPresent()) {
            return Optional.of(new TimeFrame(getEffectiveStartDateTime().get()));
        }
        else {
            return empty();
        }
    }
}
