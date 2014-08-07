package org.openmhealth.shim

import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.token.AccessTokenRequest
import org.springframework.security.oauth2.client.token.ClientTokenServices
import org.springframework.security.oauth2.client.token.RequestEnhancer
import org.springframework.util.MultiValueMap

class JawboneTokenRequestEnhancer implements RequestEnhancer {

  @Override
  void enhance(AccessTokenRequest request,
               OAuth2ProtectedResourceDetails resource,
               MultiValueMap<String, String> form,
               HttpHeaders headers) {
    /**
     * Add other jawbone required parameters for
     * getting the authentication token.
     */
    //request.set("client_id",resource.clientId)
    //request.set("client_secret",resource.clientSecret)
    //request.set("grant_type",resource.grantType)

    //request.set("code",request.getAuthorizationCode())
    //request.client_secret = [resource.clientSecret]
    //request.grant_type =["authorization_code"]

    form.set("client_id",resource.clientId)
    form.set("client_secret",resource.clientSecret)
    form.set("grant_type",resource.grantType)

    ClientTokenServices

  }
}
