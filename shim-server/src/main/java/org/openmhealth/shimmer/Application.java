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

package org.openmhealth.shimmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.openmhealth.schema.configuration.JacksonConfiguration.newObjectMapper;


/**
 * @author Emerson Farrugia
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableMongoRepositories("org.openmhealth.shim") // FIXME confirm
@ComponentScan(basePackages = {"org.openmhealth.shim", "org.openmhealth.shimmer"}) // FIXME confirm
public class Application extends WebSecurityConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // TODO refactor authentication
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        /**
         * Allow full anonymous authentication.
         */
        http.csrf().disable().authorizeRequests().anyRequest().permitAll();
    }

    // TODO look into Jackson2ObjectMapperBuilder to support Spring Boot configuration, e.g. for indentation
    @Bean
    public ObjectMapper objectMapper() {
        return newObjectMapper();
    }
}
