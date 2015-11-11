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

import org.openmhealth.shimmer.common.domain.ResponseLocation;
import org.openmhealth.shimmer.common.domain.pagination.PaginationResponseType;
import org.openmhealth.shimmer.common.domain.parameters.NumberRequestParameter;
import org.openmhealth.shimmer.common.domain.parameters.StringRequestParameter;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * @author Chris Schaefbauer
 */
public abstract class BasePaginationSettings implements PaginationSettings {

    private StringRequestParameter nextPageTokenParameter;
    private NumberRequestParameter paginationLimitParameter;
    private NumberRequestParameter paginationOffsetParameter;

    public abstract PaginationResponseType getResponseType();

    // Todo: Implement
    public String getPagingDirectionality() {

        return null;
    }

    public ResponseLocation getPaginationResponseLocation() {
        return null;
    }


    public boolean isResponseInformationEncoded() {
        return false;
    }

    public Optional<BigDecimal> getPaginationLimitDefault() {

        if (getPaginationLimitParameter().isPresent()) {
            return paginationLimitParameter.getDefaultValue();
        }

        return Optional.empty();
    }

    public Optional<BigDecimal> getPaginationMaxLimit() {
        if (getPaginationLimitParameter().isPresent()) {
            return paginationLimitParameter.getMaximumValue();
        }

        return Optional.empty();
    }

    public Optional<String> getLimitQueryParameterName() {
        return Optional.ofNullable(paginationLimitParameter.getParameterName());
    }


    public Optional<StringRequestParameter> getNextPageTokenParameter()
    {
        return Optional.ofNullable(nextPageTokenParameter);
    }


    public Optional<NumberRequestParameter> getPaginationLimitParameter() {
        return Optional.ofNullable(paginationLimitParameter);
    }


    public Optional<NumberRequestParameter> getPaginationOffsetParameter() {
        return Optional.ofNullable(paginationOffsetParameter);
    }

    public void setNextPageTokenParameter(StringRequestParameter nextPageTokenParameter) {
        this.nextPageTokenParameter = nextPageTokenParameter;
    }

    public void setPaginationLimitParameter(NumberRequestParameter paginationLimitParameter) {

        this.paginationLimitParameter = paginationLimitParameter;
    }

    public void setPaginationOffsetParameter(NumberRequestParameter paginationOffsetParameter) {
        this.paginationOffsetParameter = paginationOffsetParameter;
    }
}
