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

import org.openmhealth.shimmer.common.domain.pagination.RequestPaginationScheme;
import org.openmhealth.shimmer.common.domain.parameters.NumberRequestParameter;
import org.openmhealth.shimmer.common.domain.parameters.StringRequestParameter;

import java.util.Optional;


/**
 * Todo: Consider combing the pagination request and response config properties into one property
 * @author Chris Schaefbauer
 */
public interface PaginationRequestConfigurationProperties {

    RequestPaginationScheme getRequestPaginationScheme();

    //public boolean isPaginated();

    Optional<Integer> getPaginationLimitDefault();

    Optional<Integer> getPaginationMaxLimit();

    Optional<String> getLimitQueryParameterName();

    Optional<StringRequestParameter> getNextPageTokenParameter();

    default boolean hasPaginationLimitDefault(){ return getPaginationLimitDefault().isPresent(); }

    default boolean hasPaginationMaxLimit() { return getPaginationMaxLimit().isPresent(); }

    /* Unnecessary for the simplest solution, it supports optimization */
    String getOffsetQueryParameterName();

    Optional<NumberRequestParameter> getPaginationLimitParameter();

    Optional<NumberRequestParameter> getPaginationOffsetParameter();
}
