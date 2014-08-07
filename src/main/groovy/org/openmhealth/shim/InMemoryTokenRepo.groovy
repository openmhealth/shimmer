package org.openmhealth.shim

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.token.ClientTokenServices
import org.springframework.security.oauth2.common.OAuth2AccessToken

class InMemoryTokenRepo implements ClientTokenServices {

  private static def tokensByAuthenticationMap = [:]

  @Override
  OAuth2AccessToken getAccessToken(
          OAuth2ProtectedResourceDetails resource, Authentication authentication) {
    String principalStr = authentication.principal.toString()
    return tokensByAuthenticationMap[principalStr] ?
            (OAuth2AccessToken) tokensByAuthenticationMap[principalStr] : null
  }

  @Override
  void saveAccessToken(
          OAuth2ProtectedResourceDetails resource,
          Authentication authentication, OAuth2AccessToken accessToken) {
    String principalStr = authentication.principal.toString()
    tokensByAuthenticationMap[principalStr] = accessToken
  }

  @Override
  void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
    String principalStr = authentication.principal.toString()
    if (tokensByAuthenticationMap[principalStr]) tokensByAuthenticationMap.remove(principalStr)
  }
}
