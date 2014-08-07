package org.openmhealth.shim

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain
import org.springframework.security.oauth2.client.token.AccessTokenRequest
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.web.bind.annotation.*

@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@RestController
public class Application extends WebSecurityConfigurerAdapter {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args)
  }

  //@Value("${oauth.resource: http://localhost:8080}")
  private String baseUrl = "https://jawbone.com/nudge/api/v.1.1/users/@me/"

  //@Value("${oauth.authorize: http://localhost:8080/oauth/authorize}")
  private String authorizeUrl = "https://jawbone.com/auth/oauth2/auth"

  //@Value("${oauth.token: http://localhost:8080/oauth/token}")
  private String tokenUrl = "https://jawbone.com/auth/oauth2/token"

  public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg"

  public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19"

  public static final def JAWBONE_SCOPES = [
          "extended_read", "weight_read", "cardiac_read", "meal_read", "move_read", "sleep_read"]

  public static def ACCESS_REQUEST_REPO = [:]

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
  }

  /**
   * URL for triggering domain approval.
   * @param domain - The toolmaker domain to approve
   * @return
   */
  @RequestMapping("/authorize/{domain}")
  public ResponseEntity<String> authorize(@PathVariable("domain") String domain) {
    def restTemplate = restTemplate()
    try {
      restTemplate.getForObject(baseUrl, List.class)
    } catch (UserRedirectRequiredException e) {

      /**
       * If an exception was thrown it means a redirect is required
       * for user's external authorization with toolmaker.
       */
      def resource = (OAuth2ProtectedResourceDetails) resource()

      AccessTokenRequest accessTokenRequest =
              restTemplate.OAuth2ClientContext.accessTokenRequest
      String stateKey = accessTokenRequest.getStateKey()
      ACCESS_REQUEST_REPO[stateKey] = accessTokenRequest

      String buildUrl = "${e.redirectUri}?" +
              "state=${e.stateKey}" +
              "&client_id=${resource.clientId}" +
              "&response_type=code" +
              "&scope=${resource.scope.join(" ")}" +
              "&redirect_uri=" +
              "http://localhost:8080/authorize/jawbone/callback"
      return new ResponseEntity<String>(buildUrl, HttpStatus.OK)
    }
  }

  @RequestMapping(
          value = "/authorize/{domain}/callback",
          method = [RequestMethod.POST, RequestMethod.GET]
  )
  public ResponseEntity<String> approve(@RequestParam("state") String state,
                                        @RequestParam("code") String code) {
    def restTemplate = restTemplate(state, code)
    try {
      restTemplate.getForObject(baseUrl, List.class)
    } catch (OAuth2Exception e) {
      print "Problem trying out the token!"
      e.printStackTrace()
    }
    return new ResponseEntity<String>("Authorized", HttpStatus.OK)
  }

  @RequestMapping("/jawbone")
  public ResponseEntity<String> home() {
    @SuppressWarnings("unchecked")

    String urlRequest = baseUrl
    urlRequest += "body_events?"

    long numToReturn = 3

    long startTimeTs = 1405694496
    long endTimeTs = 1405694498

    urlRequest += "&start_time=" + startTimeTs
    urlRequest += "&end_time=" + endTimeTs
    urlRequest += "&limit=" + numToReturn

    ResponseEntity<String> response =
            restTemplate().getForEntity(urlRequest, String.class)

    return response
  }

  private OAuth2RestOperations restTemplate(String stateKey = null, String code = null) {
    def existingRequest = stateKey && ACCESS_REQUEST_REPO[stateKey] ?
            (DefaultAccessTokenRequest) ACCESS_REQUEST_REPO[stateKey] : null

    if (existingRequest && code) {
      existingRequest.set("code", code)
    }
    def context = new DefaultOAuth2ClientContext(
            existingRequest ? existingRequest : new DefaultAccessTokenRequest()
    )
    if (existingRequest) {
      context.setPreservedState(stateKey, "NONE")
    }
    def restTemplate = new OAuth2RestTemplate(resource(), context)
    def tokenProviderChain = new AccessTokenProviderChain([
            new JawboneAuthorizationCodeAccessTokenProvider(
                    tokenRequestEnhancer: new JawboneTokenRequestEnhancer()
            )]
    )
    tokenProviderChain.clientTokenServices = new InMemoryTokenRepo()
    restTemplate.accessTokenProvider = tokenProviderChain
    return restTemplate
  }

  @Bean
  protected OAuth2ProtectedResourceDetails resource() {
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