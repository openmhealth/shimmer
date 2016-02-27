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

package org.openmhealth.shimmer.common.domain.pagination;

/**
 * A type of pagination response, which dictates the how the response should be processed and used to traverse
 * pagination to retrieve all data points that satisify a request.
 *
 * @author Chris Schaefbauer
 */
public enum PaginationResponseType {

    /**
     * Indicates that a URI or URI fragment, which points to the next page of data, is returned in the response.
     */
    URI,

    /**
     * Indicates that a continuation token, which identifies the next page of data as a request parameter, is returned
     * in the response.
     */
    TOKEN,

    /**
     * Indicates that there is no specific response information used for traversing pagination, but that instead, it is
     * done using a known system of parameters for subsequent requests. For example, pagination using an skip and limit
     * parameter where the skip is incremented with each request to get the next 'limit' number of data points. In this
     * case there is an indicator that is expected in the response that identifies the situation where no further data
     * points are available.
     */
    MANUAL,

    /**
     * Indicates that a custom pagination responses is provided that does not match the other types.
     */
    CUSTOM
}
