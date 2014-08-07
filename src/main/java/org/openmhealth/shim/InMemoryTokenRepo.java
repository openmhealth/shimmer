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
        String principalStr = authentication.getPrincipal().toString();
        return tokensByAuthenticationMap.containsKey(principalStr) ?
            tokensByAuthenticationMap.get(principalStr) : null;
    }

    @Override
    public void saveAccessToken(OAuth2ProtectedResourceDetails resource,
                                Authentication authentication, OAuth2AccessToken accessToken) {
        String principalStr = authentication.getPrincipal().toString();
        tokensByAuthenticationMap.put(principalStr, accessToken);
    }

    @Override
    public void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String principalStr = authentication.getPrincipal().toString();
        if (tokensByAuthenticationMap.containsKey(principalStr))
            tokensByAuthenticationMap.remove(principalStr);
    }
}
