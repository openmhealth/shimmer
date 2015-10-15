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
public class ListRequestParameter<T> extends RequestParameter<List<T>> {

    List<T> defaultValues;
    String listDelineator;
    List<T> allowableValues;

    public Optional<List<T>> getDefaultValues() {
        return Optional.ofNullable(defaultValues);
    }

    public void setDefaultValues(List<T> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public String getListDelineator() {
        return listDelineator;
    }

    public void setListDelineator(String listDelineator) {
        this.listDelineator = listDelineator;
    }

    public Optional<List<T>> getAllowableValues() {
        return Optional.ofNullable(allowableValues);
    }

    public void setAllowableValues(List<T> allowableValues) {
        this.allowableValues = allowableValues;
    }

}
