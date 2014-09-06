/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;

import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;

/**
 * @author Danilo Bonilla
 */
public interface OAuth2Shim {

    /**
     * Request parameters to be used when 'triggering'
     * spring oauth2. This should be the equivalent
     * of a ping to the external data provider.
     *
     * @return - The Shim data request to use for trigger.
     */
    public abstract ShimDataRequest getTriggerDataRequest();

    public abstract OAuth2ProtectedResourceDetails getResource();

    public abstract AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider();
}
