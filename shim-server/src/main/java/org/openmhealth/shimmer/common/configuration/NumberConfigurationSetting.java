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

/**
 * @author Emerson Farrugia
 */
public abstract class NumberConfigurationSetting<T extends Number> extends ConfigurationSetting {

    private T minimumValue;
    private T maximumValue;

    /**
     * @return the minimum number allowed by this setting
     */
    public T getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(T minimumValue) {
        this.minimumValue = minimumValue;
    }

    /**
     * @return the maximum number allowed by this setting
     */
    public T getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(T maximumValue) {
        this.maximumValue = maximumValue;
    }
}
