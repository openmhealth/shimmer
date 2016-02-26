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

package org.openmhealth.shimmer.common.assembler;

import org.openmhealth.shimmer.common.domain.DataPointRequest;
import org.openmhealth.shimmer.common.domain.RequestEntityBuilder;


/**
 * FIXME
 *
 * @author Emerson Farrugia
 */
public interface RequestEntityAssembler {

    /**
     * Adds request parameter information and values that are necessary to fulfill the pending data point request.
     *
     * @param builder The request entity builder to assemble.
     * @param request The specification of data to be requested of a third party, which must be translated into a
     * an HTTP request that conforms to their API.
     * @return The builder with the appropriate request parameter information added. Depending on the context, it may
     * return the builder without adding any information.
     */
    RequestEntityBuilder assemble(RequestEntityBuilder builder, DataPointRequest request);
}
