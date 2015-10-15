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

package org.openmhealth.shimmer.common.domain.parameters;

import java.util.Optional;


/**
 * @author Chris Schaefbauer
 */
public abstract class RequestParameter<T> {

    private T defaultValue;
    private RequestParameterLocation requestParameterLocation;
    private boolean required;
    private String parameterName;

    public RequestParameterLocation getRequestParameterLocation() {
        return requestParameterLocation;
    }

    public void setRequestParameterLocation(
            RequestParameterLocation requestParameterLocation) {
        this.requestParameterLocation = requestParameterLocation;
    }

    public Optional<T> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
}
