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
 * Encapsulates information about how to traverse pagination from a specific endpoint for a third-party API.
 *
 * @author Chris Schaefbauer
 */
public interface PaginationSettings {

    // todo: Need to identify the getters that should be part of the interface versus pagination type-specific classes
    // Originally I included getters for properties relating to all the types (token, uri, and manual) to avoid
    // downcasting and dealing with the subclasses have more specific methods than the interface. The idea was that
    // getters that were not relevant would return empty. Once we figure out  how these pieces fit together, we can
    // likely move methods into the classes that best fit them.

    PaginationResponseType getResponseType();

    /**
     * @return The time-based directionality in which the pages of data are ordered. Could be oldest first, most recent
     * first, un-ordered, or custom.
     */
    String getPagingDirectionality();

    /**
     * @return Identifies the location, either HEADER or BODY, where pagination information will be contained in the
     * response.
     */
    ResponseLocation getPaginationResponseLocation();

    /**
     * @return TRUE if the pagination information is encoded in the response, FALSE otherwise.
     */
    boolean isResponseInformationEncoded();

    /**
     * @return The default number of data points to be returned by each response from the endpoint, if one exists.
     */
    Optional<BigDecimal> getPaginationLimitDefault();

    /**
     * @return The maximum number of data points that can be requested in each page from the endpoint, if a maximum
     * exists.
     */
    Optional<BigDecimal> getPaginationMaxLimit();

    @Deprecated
    Optional<String> getLimitQueryParameterName();

    /**
     * @return The description of the HTTP parameter used to control the maximum number of data points to be returned
     * per page, if one exists.
     */
    Optional<NumberRequestParameter> getPaginationLimitParameter();

    /**
     * @return The description of the HTTP parameter used to control the offset of data points to be skipped in
     * retrieving a specific page of data. This is specific to endpoints using offset-based manual pagination and will
     * only
     * exist for those endpoints.
     */
    Optional<NumberRequestParameter> getPaginationOffsetParameter();

    /**
     * @return The description of the HTTP parameter used in requests to capture the continuation token from the
     * previous response that identifies the next page in a token-based pagination setting, if one exists.
     */
    Optional<StringRequestParameter> getNextPageTokenParameter();

    /**
     * @return TRUE if the endpoint has a default for the number of data points returned per page, FALSE otherwise.
     */
    default boolean hasPaginationLimitDefault() {
        return getPaginationLimitDefault().isPresent();
    }

    /**
     * @return TRUE if the endpoint has a maximum for the number of data points that can be requested per page using the
     * limit parameter, FALSE otherwise.
     */
    default boolean hasPaginationMaxLimit() {
        return getPaginationMaxLimit().isPresent();
    }


    /* The following methods are unnecessary for the simplest solution where we don't worry about dealing with rate
    limits. It would support optimization and adapting the limit and the requests to optimize use of rate limits. */
    // String getOffsetQueryParameterName();

    //public String getIdentifierForNextPageProperty();

    //public T createNewResponseStrategyForType();

    // RequestPaginationScheme getRequestPaginationScheme();


}
