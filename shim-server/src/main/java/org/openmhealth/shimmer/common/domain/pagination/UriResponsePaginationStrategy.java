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

import org.openmhealth.shimmer.common.extractor.UriExtractor;


/**
 * @author Chris Schaefbauer
 */
public class UriResponsePaginationStrategy implements ResponsePaginationStrategy {


    private String paginationNextUriPropertyName;
    private String paginationBaseUri;
    private UriExtractor uriExtractor;

    public String getPaginationNextUriPropertyName() {
        return paginationNextUriPropertyName;
    }

    public void setPaginationNextUriPropertyName(String paginationNextUriPropertyName) {
        this.paginationNextUriPropertyName = paginationNextUriPropertyName;
    }

    public String getPaginationBaseUri() {
        return paginationBaseUri;
    }

    public void setPaginationBaseUri(String paginationBaseUri) {
        this.paginationBaseUri = paginationBaseUri;
    }

    @Override
    public String getResponseSchemeName() {
        return "URI";
    }




}
