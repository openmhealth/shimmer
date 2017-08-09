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

package org.openmhealth.shim.fitbit;

import org.openmhealth.shimmer.configuration.DeploymentSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;


/**
 * An enhancer that adds Fitbit-specific values to an access token request, i.e. the request that exchanges an
 * authorization code for an access token. This doesn't affect the initial authorization request, i.e. the request
 * to get the authorization code in the first place.
 *
 * @see <a href="https://dev.fitbit.com/docs/oauth2/#access-token-request">Fitbit access token request documentation</a>
 * @author Emerson Farrugia
 */
@Component
public class FitbitAccessTokenRequestEnhancer implements RequestEnhancer {

    @Autowired
    private DeploymentSettings deploymentSettings;


    @Override
    public void enhance(
            AccessTokenRequest request,
            OAuth2ProtectedResourceDetails resource,
            MultiValueMap<String, String> form,
            HttpHeaders headers) {

        form.set("client_id", resource.getClientId());

        /*
           Fitbit requires the redirect_uri to be specified if it was specified in the authorization request.
           It doesn't require the state parameter, even though the documentation says otherwise.
          */
        // TODO this won't work if a redirect URL is specified in the authorization initiation request because
        // Fitbit will reject the authorization code exchange if the redirect_uri parameters of the authorization request
        // and access token request don't match. This needs to be loaded from request scope instead.
        form.set("redirect_uri", deploymentSettings.getRedirectUrl(FitbitShim.SHIM_KEY));
    }
}