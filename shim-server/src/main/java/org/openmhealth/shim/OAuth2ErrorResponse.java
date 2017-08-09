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

package org.openmhealth.shim;


import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * @author Emerson Farrugia
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1.2.1">OAuth 2.0 reference</a>
 */
public class OAuth2ErrorResponse {

    private OAuth2ErrorResponseCode errorCode;
    private String errorDescription;
    private URI errorUri;
    private String stateKey;

    /**
     * @param redirectRequest the redirect from the external data provider
     */
    public OAuth2ErrorResponse(HttpServletRequest redirectRequest) {

        String errorCode = redirectRequest.getParameter("error");
        String stateKey = redirectRequest.getParameter("state");

        checkArgument(errorCode != null && !errorCode.isEmpty(), "The request is missing an error code.");
        checkArgument(stateKey != null && !stateKey.isEmpty(), "The request is missing a state key.");

        this.errorCode = OAuth2ErrorResponseCode.getByQueryStringValue(errorCode);
        this.errorDescription = redirectRequest.getParameter("error_description");

        String errorUriAsString = redirectRequest.getParameter("error_uri");

        if (errorUriAsString != null && !errorUriAsString.isEmpty()) {
            this.errorUri = URI.create(errorUriAsString);
        }

        this.stateKey = redirectRequest.getParameter("state");
    }

    public OAuth2ErrorResponseCode getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(OAuth2ErrorResponseCode errorCode) {

        this.errorCode = errorCode;
    }

    public Optional<String> getErrorDescription() {

        return Optional.ofNullable(errorDescription);
    }

    public void setErrorDescription(String errorDescription) {

        this.errorDescription = errorDescription;
    }

    public Optional<URI> getErrorUri() {

        return Optional.ofNullable(errorUri);
    }

    public void setErrorUri(URI errorUri) {

        this.errorUri = errorUri;
    }

    public String getStateKey() {

        return stateKey;
    }

    public void setStateKey(String stateKey) {

        this.stateKey = stateKey;
    }
}
