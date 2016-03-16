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

import java.util.Optional;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitTestProperties {

    private String startDateTime;
    private String endDateTime;
    private String sourceOriginId;
    private double fpValue;
    private DataPointModality modality;
    private String stringValue;
    private long intValue;
    private SchemaId bodySchemaId;

    public void addFloatingPointProperty(double fpVal) {

        fpValue = fpVal;
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

    public Optional<String> getEndDateTime() {

        return Optional.ofNullable(endDateTime);
    }

    public Optional<String> getStartDateTime() {

        return Optional.ofNullable(startDateTime);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
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
