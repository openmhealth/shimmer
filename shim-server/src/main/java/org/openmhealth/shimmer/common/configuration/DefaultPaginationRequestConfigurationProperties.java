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
 * @author Chris Schaefbauer
 */
public class DefaultPaginationRequestConfigurationProperties implements PaginationRequestConfigurationProperties {

    private RequestPaginationScheme requestPaginationScheme;

    private Integer paginationLimitDefault;
    private Integer paginationMaxLimit;
    private String limitQueryParameterName;
    private String offsetQueryParameterName;
    private StringRequestParameter nextPageTokenParameter;
    private NumberRequestParameter paginationLimitParameter;
    private NumberRequestParameter paginationOffsetParameter;

    @Override
    public RequestPaginationScheme getRequestPaginationScheme() {
        return this.requestPaginationScheme;
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
    public Optional<StringRequestParameter> getNextPageTokenParameter() {
        return Optional.ofNullable(nextPageTokenParameter);
    }

    @Override
    public String getOffsetQueryParameterName() {
        return this.offsetQueryParameterName;
    }

    @Override
    public Optional<NumberRequestParameter> getPaginationLimitParameter() {
        return Optional.ofNullable(paginationLimitParameter);
    }

    @Override
    public Optional<NumberRequestParameter> getPaginationOffsetParameter() {
        return Optional.ofNullable(paginationOffsetParameter);
    }

    public void setPaginationLimitDefault(Integer paginationLimitDefault) {
        this.paginationLimitDefault = paginationLimitDefault;
    }

    public void setPaginationMaxLimit(Integer paginationMaxLimit) {
        this.paginationMaxLimit = paginationMaxLimit;
    }

    public void setLimitQueryParameterName(String limitQueryParameterName) {
        this.limitQueryParameterName = limitQueryParameterName;
    }

    public void setOffsetQueryParameterName(String offsetQueryParameterName) {
        this.offsetQueryParameterName = offsetQueryParameterName;
    }

    public void setRequestPaginationScheme(
            RequestPaginationScheme requestPaginationScheme) {
        this.requestPaginationScheme = requestPaginationScheme;
    }

    public void setNextPageTokenParameter(
            StringRequestParameter nextPageTokenParameter) {
        this.nextPageTokenParameter = nextPageTokenParameter;
    }

    public void setPaginationLimitParameter(
            NumberRequestParameter paginationLimitParameter) {

        this.paginationLimitParameter = paginationLimitParameter;
        paginationLimitParameter.getDefaultValue().ifPresent(plp -> this.paginationLimitDefault = plp.intValue());
        paginationLimitParameter.getMaximumValue().ifPresent(pml -> this.paginationMaxLimit = pml.intValue());
    }

    public void setPaginationOffsetParameter(
            NumberRequestParameter paginationOffsetParameter) {
        this.paginationOffsetParameter = paginationOffsetParameter;
    }
}
