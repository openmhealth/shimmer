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

package org.openmhealth.shimmer.common.configuration;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * @author Emerson Farrugia
 */
public abstract class ConfigurationSetting {

    private String id;
    private Boolean required;
    private String label;
    private String description;

    /**
     * @return the identifier of this setting
     */
    @NotNull
    @Size(min = 1, max = 100)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return true if a value must be specified for this setting, or false otherwise
     */
    @NotNull
    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @return a short label describing the value
     */
    @NotNull
    @Size(min = 1, max = 50)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return a full description of the value
     */
    @Size(min = 1, max = 1_000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the type of value this setting configures
     */
    @NotNull
    public abstract String getValueType();
}
