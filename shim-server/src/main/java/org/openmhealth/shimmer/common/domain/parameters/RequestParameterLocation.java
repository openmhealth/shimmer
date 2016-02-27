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

package org.openmhealth.shimmer.common.domain.parameters;


/**
 * The location of a parameter in an HTTP request.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public enum RequestParameterLocation {

    PATH_VARIABLE,
    QUERY_PARAMETER,
    HEADER_FIELD
    // MATRIX_VARIABLE could be added in the future assuming the location accounts for the path element it applies to
}
