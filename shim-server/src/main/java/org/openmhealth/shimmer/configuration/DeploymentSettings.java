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
 */

package org.openmhealth.shimmer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonMap;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;


/**
 * @author Emerson Farrugia
 */
@Component
@ConfigurationProperties("openmhealth.shimmer")
public class DeploymentSettings {

    /**
     * this url will be used to create the callback url which the shim provider will call after authentication.
     */
    private String dataProviderRedirectBaseUrl;

    /**
     * Redirect url called from callback to redirect user to a custom page
     */
    private String clientRedirectUrl;

    /**
     * @return the base of the URL that data providers redirect user-agents to after an authorization
     */
    public String getDataProviderRedirectBaseUrl() {

        return dataProviderRedirectBaseUrl;
    }

    public void setDataProviderRedirectBaseUrl(String dataProviderRedirectBaseUrl) {

        this.dataProviderRedirectBaseUrl = dataProviderRedirectBaseUrl;
    }

    public String getRedirectUrl(String shimKey, String stateKey) {

        checkNotNull(shimKey, "A shim key hasn't been specified.");
        checkNotNull(stateKey, "A state key hasn't been specified.");

        return fromUriString(getRedirectUrl(shimKey))
                .queryParam("state", stateKey)
                .build()
                .toUriString();
    }

    public String getRedirectUrl(String shimKey) {

        checkNotNull(shimKey, "A shim key hasn't been specified.");

        return fromUriString(getDataProviderRedirectBaseUrl())
                .path("/authorize/{shimKey}/callback")
                .buildAndExpand(singletonMap("shimKey", shimKey))
                .toUriString();
    }

    public String getClientRedirectUrl() {
        return clientRedirectUrl;
    }

    public void setClientRedirectUrl(String clientRedirectUrl) {
        this.clientRedirectUrl = clientRedirectUrl;
    }
}