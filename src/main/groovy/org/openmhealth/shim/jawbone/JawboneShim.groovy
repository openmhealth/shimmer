package org.openmhealth.shim.jawbone

import org.openmhealth.shim.AuthorizationRequestParameters
import org.openmhealth.shim.OAuth2Shim
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException
import org.springframework.security.oauth2.client.token.AccessTokenRequest
import org.springframework.security.oauth2.client.token.RequestEnhancer
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.util.MultiValueMap

/**
 * Encapsulates parameters specific to jawbone api.
 */
class JawboneShim implements OAuth2Shim {

  private String baseUrl = "https://jawbone.com/nudge/api/v.1.1/users/@me/"

  private String authorizeUrl = "https://jawbone.com/auth/oauth2/auth"

  private String tokenUrl = "https://jawbone.com/auth/oauth2/token"

  public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg"

  public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19"

  public static final def JAWBONE_SCOPES = [
          "extended_read", "weight_read", "cardiac_read", "meal_read", "move_read", "sleep_read"]

  public void trigger(OAuth2RestOperations restTemplate) {
    getData(restTemplate, null)
  }

  //TODO: Update the method signature of this!!
  public ResponseEntity<Object> getData(OAuth2RestOperations restTemplate, Map<String, Object> params) {
    String urlRequest = baseUrl
    urlRequest += "body_events?"

    long numToReturn = 3

    long startTimeTs = 1405694496
    long endTimeTs = 1405694498

    urlRequest += "&start_time=" + startTimeTs
    urlRequest += "&end_time=" + endTimeTs
    urlRequest += "&limit=" + numToReturn

    return restTemplate.getForEntity(urlRequest, String.class)
  }

  @Override
  AuthorizationRequestParameters getAuthorizationRequestParameters(
          UserRedirectRequiredException exception) {
    def resource = getResource()

    String authorizationUrl = "${exception.redirectUri}?" +
            "state=${exception.stateKey}" +
            "&client_id=${resource.clientId}" +
            "&response_type=code" +
            "&scope=${resource.scope.join(" ")}" +
            "&redirect_uri=" +
            "http://localhost:8080/authorize/jawbone/callback" //TODO: Move this to outside

    return new AuthorizationRequestParameters(
            redirectUri: exception.redirectUri,
            stateKey: exception.stateKey,
            httpMethod: HttpMethod.GET,
            authorizationUrl: authorizationUrl
    )
  }

  public OAuth2ProtectedResourceDetails getResource() {
    def resource = new AuthorizationCodeResourceDetails()
    resource.accessTokenUri = tokenUrl
    resource.userAuthorizationUri = authorizeUrl
    resource.clientId = JAWBONE_CLIENT_ID
    resource.clientSecret = JAWBONE_CLIENT_SECRET
    resource.scope = JAWBONE_SCOPES
    resource.tokenName = "access_token"
    resource.grantType = "authorization_code"
    resource.useCurrentUri = true
    return resource;
  }

  public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
    return new JawboneAuthorizationCodeAccessTokenProvider()
  }

  /**
   * Simple overrides to base spring class from oauth.
   */
  class JawboneAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {

    JawboneAuthorizationCodeAccessTokenProvider() {
      this.tokenRequestEnhancer = new JawboneTokenRequestEnhancer()
    }

    @Override
    protected HttpMethod getHttpMethod() {
      return HttpMethod.GET
    }
  }

  /**
   * Adds jawbone required parameters to authorization token requests.
   */
  private class JawboneTokenRequestEnhancer implements RequestEnhancer {

    @Override
    void enhance(AccessTokenRequest request,
                 OAuth2ProtectedResourceDetails resource,
                 MultiValueMap<String, String> form,
                 HttpHeaders headers) {

      form.set("client_id", resource.clientId)
      form.set("client_secret", resource.clientSecret)
      form.set("grant_type", resource.grantType)
    }
  }
}
