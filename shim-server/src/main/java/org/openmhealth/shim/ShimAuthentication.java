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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;

/**
 * Bare bones authentication required by spring security.
 *
 * @author Danilo Bonilla
 */
public class ShimAuthentication implements Authentication {

    private String username;

    private String shim;

    public static final GrantedAuthority SHIM_ACCESS = new SimpleGrantedAuthority("SHIM_ACCESS");

    public ShimAuthentication(String username, String shim) {
        this.username = username;
        this.shim = shim;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(SHIM_ACCESS);
    }

    @Override
    public Object getCredentials() {
        return "none";
    }

    @Override
    public Object getDetails() {
        return shim;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {
        //noop
    }
}
