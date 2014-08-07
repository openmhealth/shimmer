package org.openmhealth.shim

import org.springframework.security.oauth2.client.OAuth2RestTemplate

import javax.servlet.http.HttpServletRequest

public interface OAuth2Shim {

  AuthorizationRequestParameters getAuthorizationRequestParameters()

  void grantCallbackHandler(HttpServletRequest request)

  OAuth2RestTemplate getAuthorizationRestTemplate()

  OAuth2RestTemplate getAuthorization

}