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
public class DefaultPaginationRequestConfigurationProperties implements PaginationRequestConfigurationProperties {

    private PaginationRequestScheme paginationRequestScheme;

    private Integer paginationLimitDefault;
    private Integer paginationMaxLimit;
    private String limitQueryParameterName;
    private String offsetQueryParameterName;

    @Override
    public PaginationRequestScheme getPaginationRequestScheme() {
        return this.paginationRequestScheme;
    }

    @Override
    public Optional<Integer> getPaginationLimitDefault() {
        return Optional.ofNullable(this.paginationLimitDefault);
    }

    @Override
    public Optional<Integer> getPaginationMaxLimit() {
        return Optional.ofNullable(this.paginationMaxLimit);
    }

    @Override
    public Optional<String> getLimitQueryParameterName() {
        return Optional.ofNullable(this.limitQueryParameterName);
    }

    @Override
    public String getOffsetQueryParameterName() {
        return this.offsetQueryParameterName;
    }
}
