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

import org.apache.http.client.methods.RequestBuilder;
import org.openmhealth.shimmer.common.domain.ResponseLocation;
import org.openmhealth.shimmer.common.extractor.PaginationResponseExtractor;


/**
 * @author Chris Schaefbauer
 */
public abstract class UriResponsePaginationStrategy implements ResponsePaginationStrategy {


    private String paginationNextUriPropertyIdentifier;
    private String paginationBaseUri;
    private PaginationResponseExtractor paginationResponseExtractor;
    private ResponseLocation paginationResponseLocation;

    public String getPaginationNextUriPropertyIdentifier() {
        return paginationNextUriPropertyIdentifier;
    }

    public void setPaginationNextUriPropertyIdentifier(String paginationNextUriPropertyIdentifier) {
        this.paginationNextUriPropertyIdentifier = paginationNextUriPropertyIdentifier;
    }

    public String getPaginationBaseUri() {
        return paginationBaseUri;
    }

    public void setPaginationBaseUri(String paginationBaseUri) {
        this.paginationBaseUri = paginationBaseUri;
    }

    @Override
    public PaginationResponseType getPaginationResponseType() {
        return PaginationResponseType.URI;
    }

    @Override
    public ResponseLocation getPaginationResponseLocation() {
        return paginationResponseLocation;
    }

    @Override
    public abstract RequestBuilder operateOnRequest(RequestBuilder requestBuilder);

    public void setPaginationResponseLocation(
            ResponseLocation paginationResponseLocation) {
        this.paginationResponseLocation = paginationResponseLocation;
    }

    public PaginationResponseExtractor getPaginationResponseExtractor() {
        return paginationResponseExtractor;
    }

    public void setPaginationResponseExtractor(PaginationResponseExtractor paginationResponseExtractor) {
        this.paginationResponseExtractor = paginationResponseExtractor;
    }


}
