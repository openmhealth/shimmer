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

import java.util.List;
import java.util.Optional;


/**
 * @author Chris Schaefbauer
 */
public class NumberRequestParameter extends RequestParameter<Double> {

    private Double maximumValue;
    private Double minimumValue;
    private List<Double> allowableValues;

    public Optional<Double> getMaximumValue() {
        return Optional.ofNullable(maximumValue);
    }

    public void setMaximumValue(Double maximumValue) {
        this.maximumValue = maximumValue;
    }

    public Optional<Double> getMinimumValue() {
        return Optional.ofNullable(minimumValue);
    }

    public void setMinimumValue(Double minimumValue) {
        this.minimumValue = minimumValue;
    }


    public Optional<List<Double>> getAllowableValues() {
        return Optional.ofNullable(allowableValues);
    }

    public void setAllowableValues(List<Double> allowableValues) {
        this.allowableValues = allowableValues;
    }
}
