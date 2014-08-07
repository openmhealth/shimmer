package org.openmhealth.shim;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;

import java.util.Map;

public interface OAuth2Shim {

    public abstract OAuth2ProtectedResourceDetails getResource();

    public abstract void trigger(OAuth2RestOperations restTemplate);

    public abstract AuthorizationRequestParameters getAuthorizationRequestParameters(UserRedirectRequiredException exception);

    public abstract ResponseEntity<String> getData(OAuth2RestOperations restTemplate, Map<String, Object> params);

    public abstract AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider();
}
