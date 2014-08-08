package org.openmhealth.shim;


import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.LinkedHashMap;

public class InMemoryTokenRepo implements ClientTokenServices {

    private static LinkedHashMap<String, OAuth2AccessToken> tokensByAuthenticationMap =
        new LinkedHashMap<String, OAuth2AccessToken>();

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2ProtectedResourceDetails resource,
                                            Authentication authentication) {
        String tokenKey = getTokenKey(authentication);
        return tokensByAuthenticationMap.containsKey(tokenKey) ?
            tokensByAuthenticationMap.get(tokenKey) : null;
    }

    @Override
    public void saveAccessToken(OAuth2ProtectedResourceDetails resource,
                                Authentication authentication, OAuth2AccessToken accessToken) {
        String tokenKey = getTokenKey(authentication);
        tokensByAuthenticationMap.put(tokenKey, accessToken);
    }

    @Override
    public void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String tokenKey = getTokenKey(authentication);
        if (tokensByAuthenticationMap.containsKey(tokenKey))
            tokensByAuthenticationMap.remove(tokenKey);
    }

    private String getTokenKey(Authentication authentication) {
        return authentication.getPrincipal().toString()
            + ":" + authentication.getDetails();
    }
}
