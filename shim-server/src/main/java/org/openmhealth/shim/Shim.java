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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * High level abstraction contract for shims. This version
 * does not take into account any details (oauth, oauth2, custom, etc)
 *
 * @author Danilo Bonilla
 */
public interface Shim {

    /**
     * returns a unique string identifier for the shim.
     *
     * @return - value of identifier ('jawbone', 'fitbit', etc).
     */
    String getShimKey();

    /**
     * Base of the URL to which the user will
     * be sent to authenticate.
     *
     * @return - Absolute URL for authorizing.
     */
    String getUserAuthorizationUrl();

    /**
     * Base of the URL queried to request an access token.
     *
     * @return - Absolute URL for getting an access token
     */
    String getAccessTokenUrl();

    /**
     * Get a list of the shim's data types.
     *
     * @return Data types supported by the shim.
     */
    ShimDataType[] getShimDataTypes();

    /**
     * A formal display label for the shim.
     *
     * @return -  The label.
     */
    String getLabel();

    /**
     * Retrieve authorization parameter object so that an external
     * endpoint can take control of the flow.
     *
     * @param additionalParameters - any parameters that should be added to the authorization request.
     * @return AuthorizationParameters needed to iniate oauth flow.
     */
    AuthorizationRequestParameters getAuthorizationRequestParameters(
            final String username,
            final Map<String, String> additionalParameters)
            throws ShimException;

    /**
     * Handles a redirect from an external data provider.
     *
     * @param servletRequest - the HTTP request corresponding to the redirect
     * @return Authorization response
     */
    AuthorizationResponse processRedirect(
            final HttpServletRequest servletRequest)
            throws ShimException;

    /**
     * Obtain data from the external data provider using access parameters
     *
     * @param shimDataRequest - Data request to be full-filled by the shim.
     * @return Generic object wrapper including timestamp, shim, and results
     */
    ShimDataResponse getData(final ShimDataRequest shimDataRequest) throws ShimException;


    /**
     * Checks if this shim is properly configured.
     *
     * @return true if this shim is properly configured.
     */
    boolean isConfigured();
}
