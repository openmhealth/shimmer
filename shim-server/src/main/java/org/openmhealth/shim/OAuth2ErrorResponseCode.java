/*
 * Copyright 2017 Open mHealth
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
 *
 */

package org.openmhealth.shim;

import static java.lang.String.format;


/**
 * @author Emerson Farrugia
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1.2.1">OAuth 2.0 reference</a>
 */
public enum OAuth2ErrorResponseCode {

    INVALID_REQUEST,
    UNAUTHORIZED_CLIENT,
    ACCESS_DENIED,
    UNSUPPORTED_RESPONSE_TYPE,
    INVALID_SCOPE,
    SERVER_ERROR,
    TEMPORARILY_UNVAILABLE;


    public static OAuth2ErrorResponseCode getByQueryStringValue(String value) {

        for (OAuth2ErrorResponseCode code : values()) {
            if (value.equalsIgnoreCase(code.name())) {
                return code;
            }
        }

        throw new IllegalArgumentException(
                format("An error response code with query string value '{}' doesn't exist.", value));
    }
}