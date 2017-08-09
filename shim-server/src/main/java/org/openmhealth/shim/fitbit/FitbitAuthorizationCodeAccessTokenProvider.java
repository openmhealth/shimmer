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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.util.List;


/**
 * An provider that exchanges a Fitbit authorization code for an access token.
 *
 * @author Emerson Farrugia
 */
@Component
public class FitbitAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(FitbitAuthorizationCodeAccessTokenProvider.class);

    @Autowired
    public FitbitAuthorizationCodeAccessTokenProvider(
            FitbitAccessTokenRequestEnhancer tokenRequestEnhancer,
            ClientHttpRequestFactory requestFactory) {

        setTokenRequestEnhancer(tokenRequestEnhancer);
        setRequestFactory(requestFactory);
    }

    /**
     * Add any interceptors found in the application context, e.g. for logging, which may or may not be present
     * based on profiles.
     */
    @Autowired(required = false)
    @Override
    public void setInterceptors(List<ClientHttpRequestInterceptor> interceptors) {

        if (interceptors.isEmpty()) {
            logger.info("No HTTP request interceptors have been found in the application context.");
            return;
        }

        for (ClientHttpRequestInterceptor interceptor : interceptors) {
            logger.info("The interceptor '{}' will be added to this provider.", interceptor.getClass().getSimpleName());
        }

        super.setInterceptors(interceptors);
    }

    @Override
    protected HttpMethod getHttpMethod() {

        return HttpMethod.POST;
    }

    @Override
    protected ResponseErrorHandler getResponseErrorHandler() {

        return super.getResponseErrorHandler();
    }
}