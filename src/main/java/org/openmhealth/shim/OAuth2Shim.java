package org.openmhealth.shim;

import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;

public interface OAuth2Shim {

    public abstract OAuth2ProtectedResourceDetails getResource();

    //public abstract void trigger(OAuth2RestOperations restTemplate);

    public abstract AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider();
}
