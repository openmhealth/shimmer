package org.openmhealth.shim

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain
import org.springframework.security.oauth2.client.token.AccessTokenRequest
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest
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

  private JawboneShim jawboneShim = new JawboneShim()

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
      jawboneShim.trigger(restTemplate)
      return new ResponseEntity<String>("Authorized", HttpStatus.OK)
    } catch (UserRedirectRequiredException e) {
      /**
       * If an exception was thrown it means a redirect is required
       * for user's external authorization with toolmaker.
       */
      AccessTokenRequest accessTokenRequest =
              restTemplate.OAuth2ClientContext.accessTokenRequest
      String stateKey = accessTokenRequest.getStateKey()
      ACCESS_REQUEST_REPO[stateKey] = accessTokenRequest
      return new ResponseEntity<String>(
              jawboneShim.getAuthorizationRequiredUrl(e), HttpStatus.OK)
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
      jawboneShim.trigger(restTemplate)
    } catch (OAuth2Exception e) {
      print "Problem trying out the token!"
      e.printStackTrace()
    }
    return new ResponseEntity<String>("Authorized", HttpStatus.OK)
  }

  @RequestMapping("/jawbone")
  public ResponseEntity<String> home() {
    return jawboneShim.getData(restTemplate())
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
    def restTemplate = new OAuth2RestTemplate(jawboneShim.getResource(), context)
    def tokenProviderChain = new AccessTokenProviderChain([
            new JawboneAuthorizationCodeAccessTokenProvider(
                    tokenRequestEnhancer: new JawboneTokenRequestEnhancer()
            )]
    )
    tokenProviderChain.clientTokenServices = new InMemoryTokenRepo()
    restTemplate.accessTokenProvider = tokenProviderChain
    return restTemplate
  }
}