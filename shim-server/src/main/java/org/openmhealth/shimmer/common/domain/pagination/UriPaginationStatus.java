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
 * @author Chris Schaefbauer
 */
public class UriPaginationStatus implements PaginationStatus {

    private String nextPageUriValue;
    //private String baseUri;

    @Override
    public boolean hasMoreData() {
        return getPaginationResponseValue().isPresent();
    }

//    public String getBaseUri() {
//        return baseUri;
//    }
//
//    public void setBaseUri(String baseUri) {
//        this.baseUri = baseUri;
//    }

    public Optional<String> getPaginationResponseValue() {
        return Optional.ofNullable(nextPageUriValue);
    }

    public void setPaginationResponseValue(String paginationNextUriValue) {
        this.nextPageUriValue = paginationNextUriValue;
    }

//    @Override
//    public UriResponsePaginationStrategy createNewResponseStrategyForType(
//            UriPaginationSettings configuration) {
//
//        if(configuration.getBaseUri().isPresent() && !configuration.getBaseUri().get().isEmpty()){
//            return new ConcatUriResponsePaginationStrategy();
//        }
//
//        return new CompleteUriResponsePaginationStrategy();
//    }



}
