package org.openmhealth.shim

import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider

public interface OAuth2Shim {

  OAuth2ProtectedResourceDetails getResource()

  void trigger(OAuth2RestOperations restTemplate)

  AuthorizationRequestParameters getAuthorizationRequestParameters(UserRedirectRequiredException exception)

  ResponseEntity<Object> getData(OAuth2RestOperations restTemplate, Map<String, Object> params)

  AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider()
}