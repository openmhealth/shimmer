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

import org.openmhealth.shimmer.common.domain.PaginationRequestScheme;

import java.util.Optional;


/**
 * @author Chris Schaefbauer
 */
public interface PaginationRequestConfigurationProperties {

    PaginationRequestScheme getPaginationRequestScheme();

    //public boolean isPaginated();

    public Optional<Integer> getPaginationLimitDefault();

    public Optional<Integer> getPaginationMaxLimit();

    public Optional<String> getLimitQueryParameterName();

    public default boolean hasPaginationLimitDefault(){ return getPaginationLimitDefault().isPresent(); }

    public default boolean hasPaginationMaxLimit() { return getPaginationMaxLimit().isPresent(); }

    /* Unnecessary for the simplest solution, it supports optimization */
    public String getOffsetQueryParameterName();

}
