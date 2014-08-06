package org.openmhealth.shim

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain
import org.springframework.security.oauth2.client.token.AccessTokenRequest
import org.springframework.security.oauth2.client.token.ClientTokenServices
import org.springframework.security.oauth2.client.token.JdbcClientTokenServices
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.common.AuthenticationScheme
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.annotation.Resource

@Configuration
@EnableAutoConfiguration
@EnableOAuth2Client
@RestController
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  //@Value("${oauth.resource: http://localhost:8080}")
  private String baseUrl = "https://jawbone.com/nudge/api/v.1.1/users/@me/";

  //@Value("${oauth.authorize: http://localhost:8080/oauth/authorize}")
  private String authorizeUrl = "https://jawbone.com/auth/oauth2/auth";

  //@Value("${oauth.token: http://localhost:8080/oauth/token}")
  private String tokenUrl = "https://jawbone.com/auth/oauth2/token";

  public static final String JAWBONE_CLIENT_ID = "q3Nsl0zMbkg";

  public static final String JAWBONE_CLIENT_SECRET = "ed722cc43adce63f5abb34bf7bc5485132bd2a19";

  public static final def JAWBONE_SCOPES = [
          "extended_read", "weight_read", "cardiac_read", "meal_read", "move_read", "sleep_read"]

  @Resource
  @Qualifier("accessTokenRequest")
  private AccessTokenRequest accessTokenRequest;

  @RequestMapping("/jawbone")
  public List<Map<String, ?>> home() {
    @SuppressWarnings("unchecked")

    String urlRequest = baseUrl
    urlRequest += "body_events?"

    long numToReturn = 3

    long startTimeTs = 1405694496
    long endTimeTs = 1405694498

    urlRequest += "&start_time=" + startTimeTs
    urlRequest += "&end_time=" + endTimeTs
    urlRequest += "&limit=" + numToReturn

    List<Map<String, ?>> result = restTemplate().getForObject(urlRequest,List.class)

    return result;
  }

  @Bean
  @Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
  public OAuth2RestOperations restTemplate() {
    OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource(),
            new DefaultOAuth2ClientContext(accessTokenRequest))
    restTemplate.accessTokenProvider = new AccessTokenProviderChain([
            new JawboneAuthorizationCodeAccessTokenProvider(
                    tokenRequestEnhancer: new JawboneTokenRequestEnhancer()
            )])
    return restTemplate
  }

  @Bean
  protected OAuth2ProtectedResourceDetails resource() {
    AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails()
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