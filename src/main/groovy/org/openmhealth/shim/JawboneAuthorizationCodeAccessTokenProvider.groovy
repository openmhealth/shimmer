package org.openmhealth.shim

import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider

/**
 * Created by danilobonilla on 8/6/14.
 */
class JawboneAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider{

  @Override
  protected HttpMethod getHttpMethod() {
    return HttpMethod.GET
  }

}
