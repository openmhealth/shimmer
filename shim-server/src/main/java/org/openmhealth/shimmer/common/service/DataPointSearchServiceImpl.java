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

package org.openmhealth.shimmer.common.service;

import org.openmhealth.shimmer.common.domain.DataPointSearchCriteria;
import org.openmhealth.shimmer.common.domain.DataPointSearchResult;
import org.springframework.stereotype.Service;


/**
 * An implementation that assumes data points can be retrieved from third-parties by making requests to specific API
 * endpoints.
 *
 * @author Emerson Farrugia
 */
@Service
public class DataPointSearchServiceImpl implements DataPointSearchService {

    @Override
    public DataPointSearchResult findDataPoints(DataPointSearchCriteria criteria) {

        // TODO implement me
        return new DataPointSearchResult();
    }
}