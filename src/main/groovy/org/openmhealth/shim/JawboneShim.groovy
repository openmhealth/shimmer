package org.openmhealth.shim

import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails

/**
 * Created by danilobonilla on 8/6/14.
 */
class JawboneShim {

  private String baseUrl = "https://jawbone.com/nudge/api/v.1.1/users/@me/"

  private String authorizeUrl = "https://jawbone.com/auth/oauth2/auth"

  private String tokenUrl = "https://jawbone.com/auth/oauth2/token"

  public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg"

  public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19"

  public static final def JAWBONE_SCOPES = [
          "extended_read", "weight_read", "cardiac_read", "meal_read", "move_read", "sleep_read"]

  public void trigger(OAuth2RestOperations restTemplate) {
    /*String pingUrl = baseUrl + ""
    restTemplate.getForEntity(pingUrl, String.class)*/
    //todo: This should call a URL with little to no data
    getData(restTemplate)
  }

  //TODO: Update the method signature of this!!
  public ResponseEntity<String> getData(OAuth2RestOperations restTemplate) {
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

  public String getAuthorizationRequiredUrl(UserRedirectRequiredException exception) {
    def resource = getResource()
    return "${exception.redirectUri}?" +
            "state=${exception.stateKey}" +
            "&client_id=${resource.clientId}" +
            "&response_type=code" +
            "&scope=${resource.scope.join(" ")}" +
            "&redirect_uri=" +
            "http://localhost:8080/authorize/jawbone/callback" //TODO: Move this to outside
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
}
