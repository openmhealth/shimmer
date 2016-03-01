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
import org.openmhealth.shimmer.common.domain.pagination.PaginationResponseEncoding;
import org.openmhealth.shimmer.common.domain.pagination.PaginationResponseType;
import org.openmhealth.shimmer.common.domain.parameters.NumberRequestParameter;
import org.openmhealth.shimmer.common.domain.parameters.StringRequestParameter;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * Provides a default implementation for common pagination settings.
 *
 * @author Chris Schaefbauer
 */
public abstract class BasePaginationSettings implements PaginationSettings {


    private StringRequestParameter nextPageTokenParameter;
    private NumberRequestParameter paginationLimitParameter;
    private NumberRequestParameter paginationOffsetParameter;
    private ResponseLocation paginationResponseLocation;
    private PaginationResponseEncoding responseEncoding;
    private String nextPagePropertyIdentifier;

    public abstract PaginationResponseType getResponseType();

    // Todo: Implement
    public String getPagingDirectionality() {

        return null;
    }


    public boolean isResponseInformationEncoded() {
        return false;
    }

    @Override
    public Optional<BigDecimal> getPaginationLimitDefault() {

        if (getPaginationLimitParameter().isPresent()) {
            return paginationLimitParameter.getDefaultValue();
        }

        return Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getPaginationMaxLimit() {
        if (getPaginationLimitParameter().isPresent()) {
            return paginationLimitParameter.getMaximumValue();
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> getLimitQueryParameterName() {
        return Optional.ofNullable(paginationLimitParameter.getName());
    }

    @Override
    public Optional<StringRequestParameter> getNextPageTokenParameter() {
        return Optional.ofNullable(nextPageTokenParameter);
    }

    @Override
    public Optional<NumberRequestParameter> getPaginationLimitParameter() {
        return Optional.ofNullable(paginationLimitParameter);
    }

    @Override
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

    @Override
    public ResponseLocation getPaginationResponseLocation() {
        return paginationResponseLocation;
    }

    public void setPaginationResponseLocation(ResponseLocation location) {
        this.paginationResponseLocation = location;
    }

    @Override
    public Optional<PaginationResponseEncoding> getResponseEncodingType() {
        return Optional.of(responseEncoding);
    }

    public void setPaginationResponseEncoding(PaginationResponseEncoding encoding){
        this.responseEncoding = encoding;
    }

    /**
     * @return The JSON dot path to the property in the response that contains the URI or URI fragment pointing to the
     * next page of data.
     */
    public String getNextPagePropertyIdentifier() {
        return nextPagePropertyIdentifier;
    }

    public void setNextPagePropertyIdentifier(String propertyIdentifier){
        this.nextPagePropertyIdentifier = propertyIdentifier;
    }
}
