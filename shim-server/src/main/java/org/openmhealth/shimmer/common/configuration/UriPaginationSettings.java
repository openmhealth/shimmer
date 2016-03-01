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

import org.openmhealth.shimmer.common.domain.pagination.PaginationResponseType;

import java.util.Optional;

import static org.openmhealth.shimmer.common.domain.pagination.PaginationResponseType.URI;


/**
 * Encapsulates information about how to traverse the pagination for an endpoint where the endpoint provides a URI or
 * URI-fragment in the response that is used to retrieve the next page.
 *
 * @author Chris Schaefbauer
 */
public class UriPaginationSettings extends BasePaginationSettings {

    private String baseUri;

    /**
     * @return TRUE if paginated responses return a full URI pointing to the next page when more data points are
     * available via pagination, FALSE if paginated responses return a partial URI fragment when more data points are
     * available.
     */
    public boolean providesCompleteUri() {
        return !getBaseUri().isPresent();
    }

    /**
     * todo: Should rename this to be getUriTemplateForPaginatedRequest or something similar
     *
     * @return Returns the URI template that surrounds the URI fragment returned in the response when more pages of data
     * are available, if one exists. This will be empty if the response contains the entire URI instead of a fragment.
     */
    public Optional<String> getBaseUri() {
        return Optional.ofNullable(baseUri);
    }

    /**
     * Sets the value of the base URI template for which a fragment should be embedded, if one exists for the endpoint.
     *
     * @param baseUri The base URI template.
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public PaginationResponseType getResponseType() {
        return URI;
    }



}
