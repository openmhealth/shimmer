package org.openmhealth.shimmer.common.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * TODO this is just a refactoring of existing code, needs to be revised
 *
 * @author Emerson Farrugia
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        /**
         * Allow full anonymous authentication.
         */
        http.csrf().disable().authorizeRequests().anyRequest().permitAll();
    }
}
