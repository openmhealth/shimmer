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

package org.openmhealth.shimmer.common.configuration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;


/**
 * A configuration for an Apache HttpComponents request factory that enables connection pooling, timeouts, and
 * potentially request logging.
 *
 * @author Emerson Farrugia
 */
@Configuration
public class HttpClientConfiguration {

    @Bean
    public HttpClient httpClient() {

        return HttpClientBuilder.create()
                .setMaxConnPerRoute(20) // chosen without much thought, refine at will
                .setMaxConnTotal(100)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient());

        factory.setReadTimeout(30_000); // chosen without much thought, refine at will
        factory.setConnectTimeout(5_000);

        return factory;
    }
}
