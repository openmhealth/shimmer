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
 * A parameter sent as part of an HTTP request.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class RequestParameter<T> {

    private String name;
    private RequestParameterLocation location;
    private T defaultValue;
    private boolean required;

    /**
     * @return the name of the parameter. This doubles as the field name for query parameters and header fields, and the
     * variable name for path variables.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name of the parameter
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the location of the parameter
     */
    public RequestParameterLocation getLocation() {
        return location;
    }

    /**
     * @param location the location of the parameter
     */
    public void setLocation(RequestParameterLocation location) {
        this.location = location;
    }

    /**
     * @return the default value of the parameter
     */
    public Optional<T> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    /**
     * @param defaultValue the default value of the parameter
     */
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return true if the parameter is required, or false otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required true if the parameter is required, or false otherwise
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @param value a parameter value
     * @return the value in a format suitable to be added to the request
     */
    public String newArgument(T value) {
        return value.toString();
    }
}
