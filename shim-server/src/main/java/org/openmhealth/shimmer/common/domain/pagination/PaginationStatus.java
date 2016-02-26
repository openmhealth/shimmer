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

import java.util.Optional;


/**
 * Encapsulates the information necessary to determine whether more information exists through pagination and how to
 * follow that information to retrieve additional data.
 *
 * @author Chris Schaefbauer
 */
public interface PaginationStatus {

    /**
     * @return TRUE if there is more data retrievable through pagination, FALSE if there is no additional data
     */
    boolean hasMoreData();

    // Todo: Consider renaming so that it also would make sense for

    /**
     * @return The value returned in the response from the third-party API during a previous request, if one was
     * provided. Otherwise, empty will be returned.
     */
    Optional<String> getPaginationResponseValue();

    /**
     * Sets the value for the next page reference.
     *
     * @param nextPageValueFromResponse Value from the third-party API response that contains information necessary to
     * retrieve the next page of data points.
     */
    void setPaginationResponseValue(String nextPageValueFromResponse);

}
