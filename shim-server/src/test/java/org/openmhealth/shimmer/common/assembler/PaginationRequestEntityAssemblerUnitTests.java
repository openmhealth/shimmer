/*
 * Copyright 2016 Open mHealth
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

package org.openmhealth.shimmer.common.assembler;

import org.openmhealth.shimmer.common.configuration.EndpointSettings;
import org.openmhealth.shimmer.common.domain.DataPointRequest;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;


/**
 * @author Chris Schaefbauer
 */
public class PaginationRequestEntityAssemblerUnitTests {

    public DataPointRequest createTestDataPointRequest(EndpointSettings configurationProperties,
            PaginationStatus paginationStatus) {

        DataPointRequest dataPointRequest =
                new DataPointRequest(configurationProperties, "testUser", "testNamespace", "testSchemaName", "1.0");

        if (paginationStatus != null) {
            dataPointRequest.setPaginationStatus(paginationStatus);
        }

        return dataPointRequest;
    }

}
