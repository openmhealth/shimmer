package org.openmhealth.shim

import org.springframework.web.bind.annotation.RequestMethod

class AuthorizationRequestParameters {

  RequestMethod method = RequestMethod.POST

  Map<String, String> headers = [:] RequestMethod getMethod() {
    return method
  }

  void setMethod(RequestMethod method) {
    this.method = method
  }

  Map<String, String> getHeaders() {
    return headers
  }

  void setHeaders(Map<String, String> headers) {
    this.headers = headers
  }
}
