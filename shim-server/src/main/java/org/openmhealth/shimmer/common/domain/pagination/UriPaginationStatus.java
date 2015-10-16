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
public class UriPaginationStatus implements PaginationStatus<UriResponsePaginationStrategy> {

    private String nextUriStringFromResponse;
    private UriResponsePaginationStrategy uriResponsePaginationStrategy;

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    private String baseUri;

    @Override
    public boolean hasMoreData() {
        return getNextUriStringFromResponse().isPresent();
    }

    @Override
    public UriResponsePaginationStrategy getResponseStrategy() {
        return uriResponsePaginationStrategy;
    }

    @Override
    public void setResponseStrategy(UriResponsePaginationStrategy paginationResponseStrategy) {
        this.uriResponsePaginationStrategy = paginationResponseStrategy;
    }

    @Override
    public UriResponsePaginationStrategy createNewResponseStrategyForType() {
        return new UriResponsePaginationStrategy();
    }

    public Optional<String> getNextUriStringFromResponse() {
        return Optional.ofNullable(nextUriStringFromResponse);
    }

    public void setNextUriStringFromResponse(String nextUriStringFromResponse) {
        this.nextUriStringFromResponse = nextUriStringFromResponse;
    }

}
