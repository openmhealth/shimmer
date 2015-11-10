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

import java.util.Optional;

import static org.openmhealth.shimmer.common.domain.pagination.PaginationResponseType.URI;


/**
 * @author Chris Schaefbauer
 */
public class UriPaginationResponseConfigurationProperties implements PaginationResponseConfigurationProperties {

    private String baseUri;

    private String pagingDirectionality; // Todo: This needs to be broken up to address the 3 types of directionality
    private boolean responseInformationEncoded;

    public boolean providesCompleteUri(){
        return !getBaseUri().isPresent();
    }

    public Optional<String> getBaseUri() {
        return Optional.ofNullable(baseUri);
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public PaginationResponseType getResponseType() {
        return URI;
    }

    @Override
    public ResponseLocation getPaginationResponseLocation() {
        return null;
    }

    @Override
    public boolean isResponseInformationEncoded() {
        return responseInformationEncoded;
    }

    public String getNextPaginationPropertyIdentifier() {
        return null;
    }

    @Override
    public String getPagingDirectionality() {
        return pagingDirectionality;
    }

    public void setPagingDirectionality(String pagingDirectionality) {
        this.pagingDirectionality = pagingDirectionality;
    }

    public void setResponseInformationEncoded(boolean responseInformationEncoded) {
        this.responseInformationEncoded = responseInformationEncoded;
    }
}
