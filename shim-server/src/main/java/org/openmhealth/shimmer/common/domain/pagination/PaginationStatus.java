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
 * Encapsulates the information necessary to determine whether more information exists in pagination and how to follow that information.
 * @author Chris Schaefbauer
 */
public interface PaginationStatus {

    /**
     * @return whether or not there is more data to retrieve through pagination
     */
    public boolean hasMoreData();

    // Todo: Consider renaming so that it also would make sense for
    public Optional<String> getPaginationResponseValue();

    public void setPaginationResponseValue(String nextPageValueFromResponse);

//    public T getResponseStrategy();
//
//    public void setResponseStrategy(T paginationResponseStrategy);

    //public T createNewResponseStrategyForType(PaginationSettings configuration);




}
