/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;

import java.time.OffsetDateTime;


/**
 * A wrapper for encapsulating data requests sent to shims. Prevents from having long method signatures.
 *
 * @author Danilo Bonilla
 */
public class ShimDataRequest {

    /**
     * Identifier for the type of data being retrieved
     */
    private String dataTypeKey;
    /**
     * parameters required for acessing data, this will likely be oauth token + any extras or some kind of trusted
     * access.
     */
    private AccessParameters accessParameters;

    /**
     * // TODO replace this with filters on effective time, using the Data Point API The start date for the data being
     * retrieved
     */
    private OffsetDateTime startDateTime;

    /**
     * The end date for the data being retrieved
     */
    private OffsetDateTime endDateTime;

    /**
     * If true, returns normalized results from the external data provider, otherwise returns raw data.
     */
    private boolean normalize = true;

    public OffsetDateTime getStartDateTime() {

        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {

        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {

        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {

        this.endDateTime = endDateTime;
    }

    public String getDataTypeKey() {

        return dataTypeKey;
    }

    public void setDataTypeKey(String dataTypeKey) {

        this.dataTypeKey = dataTypeKey;
    }

    public AccessParameters getAccessParameters() {

        return accessParameters;
    }

    public void setAccessParameters(AccessParameters accessParameters) {

        this.accessParameters = accessParameters;
    }

    public boolean getNormalize() {

        return normalize;
    }

    public void setNormalize(boolean normalize) {

        this.normalize = normalize;
    }
}
