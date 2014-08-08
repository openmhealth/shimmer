package org.openmhealth.shim;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;

/**
 * Bare bones authentication required by spring security.
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
