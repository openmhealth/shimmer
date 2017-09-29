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

import java.time.OffsetDateTime;
import java.util.Optional;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class GoogleFitTestProperties {

    private OffsetDateTime effectiveStartDateTime;
    private OffsetDateTime effectiveEndDateTime;
    private String sourceOriginId;
    private double fpValue;
    private DataPointModality modality;
    private String stringValue;
    private long intValue;
    private SchemaId bodySchemaId;

    public void setFpValue(double fpValue) {

        this.fpValue = fpValue;
    }

    public String getSourceOriginId() {

        return sourceOriginId;
    }

    public Optional<DataPointModality> getModality() {

        return Optional.ofNullable(modality);
    }

    public void setModality(DataPointModality modality) {
        this.modality = modality;
    }

    public double getFpValue() {
        return fpValue;
    }

    public Optional<OffsetDateTime> getEffectiveEndDateTime() {

        return Optional.ofNullable(effectiveEndDateTime);
    }

    public Optional<OffsetDateTime> getEffectiveStartDateTime() {

        return Optional.ofNullable(effectiveStartDateTime);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setEffectiveStartDateTime(String effectiveStartDateTime) {
        this.effectiveStartDateTime = OffsetDateTime.parse(effectiveStartDateTime);
    }

    public void setEffectiveEndDateTime(String effectiveEndDateTime) {
        this.effectiveEndDateTime = OffsetDateTime.parse(effectiveEndDateTime);
    }

    public void setSourceOriginId(String sourceOriginId) {
        this.sourceOriginId = sourceOriginId;
    }

    public void setIntValue(long integerValue) {
        this.intValue = integerValue;
    }

    public long getIntValue() {
        return intValue;
    }

    public SchemaId getBodySchemaId() {
        return bodySchemaId;
    }

    public void setBodySchemaId(SchemaId bodySchemaId) {
        this.bodySchemaId = bodySchemaId;
    }
}
