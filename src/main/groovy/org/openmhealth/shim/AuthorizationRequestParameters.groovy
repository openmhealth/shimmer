package org.openmhealth.shim

import org.springframework.http.HttpMethod

class AuthorizationRequestParameters {

  HttpMethod httpMethod = HttpMethod.POST

  String stateKey

  String redirectUri

  Map<String, String> requestParams

  String authorizationUrl

  //TODO: May be required later
  //Map<String, String> headers

  String getAuthorizationUrl() {
    return authorizationUrl
  }

  void setAuthorizationUrl(String authorizationUrl) {
    this.authorizationUrl = authorizationUrl
  }

  HttpMethod getHttpMethod() {
    return httpMethod
  }

  void setHttpMethod(HttpMethod httpMethod) {
    this.httpMethod = httpMethod
  }

  String getStateKey() {
    return stateKey
  }

  void setStateKey(String stateKey) {
    this.stateKey = stateKey
  }

  String getRedirectUri() {
    return redirectUri
  }

  void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri
  }

  Map<String, String> getRequestParams() {
    return requestParams
  }

  void setRequestParams(Map<String, String> requestParams) {
    this.requestParams = requestParams
  }
}
