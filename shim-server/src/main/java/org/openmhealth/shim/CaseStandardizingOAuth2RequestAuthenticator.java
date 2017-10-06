/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.security.oauth2.client.DefaultOAuth2RequestAuthenticator;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RequestAuthenticator;
import org.springframework.security.oauth2.client.http.AccessTokenRequiredException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.StringUtils;


/**
 * A customization of {@link DefaultOAuth2RequestAuthenticator} that standardizes the case of the Authorization header
 * token type to "Bearer". This is necessary because the default implementation doesn't work for Moves, which serves up
 * a "bearer" token but only accepts "Bearer" authorization headers.
 *
 * @author Dave Syer
 * @author Emerson Farrugia
 */
public class CaseStandardizingOAuth2RequestAuthenticator implements OAuth2RequestAuthenticator {

    @Override
    public void authenticate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext clientContext,
            ClientHttpRequest request) {

        OAuth2AccessToken accessToken = clientContext.getAccessToken();
        if (accessToken == null) {
            throw new AccessTokenRequiredException(resource);
        }

        String tokenType = accessToken.getTokenType();

        if (!StringUtils.hasText(tokenType) || tokenType.equalsIgnoreCase(OAuth2AccessToken.BEARER_TYPE)) {
            tokenType = OAuth2AccessToken.BEARER_TYPE; // we'll assume basic bearer token type if none is specified.
        }

        request.getHeaders().set("Authorization", String.format("%s %s", tokenType, accessToken.getValue()));
    }
}
