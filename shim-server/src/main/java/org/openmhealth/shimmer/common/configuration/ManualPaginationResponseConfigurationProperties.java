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
import org.openmhealth.shimmer.common.domain.pagination.ManualPaginationEndCriteria;
import org.openmhealth.shimmer.common.domain.pagination.PaginationResponseType;

import static org.openmhealth.shimmer.common.domain.pagination.PaginationResponseType.*;


/**
 * @author Chris Schaefbauer
 */
public class ManualPaginationResponseConfigurationProperties implements PaginationResponseConfigurationProperties {

    private ManualPaginationEndCriteria paginationEndCriteria;

    @Override
    public PaginationResponseType getResponseType() {
        return MANUAL;
    }

    @Override
    public String getPagingDirectionality() {
        return null;
    }

    @Override
    public ResponseLocation getPaginationResponseLocation() {
        return null;
    }

    public ManualPaginationEndCriteria getPaginationEndCriteria() {
        return paginationEndCriteria;
    }

    @Override
    public String getNextPaginationPropertyIdentifier() {
        return null;
    }

    public void setPaginationEndCriteria(ManualPaginationEndCriteria paginationEndCriteria) {
        this.paginationEndCriteria = paginationEndCriteria;
    }
}
