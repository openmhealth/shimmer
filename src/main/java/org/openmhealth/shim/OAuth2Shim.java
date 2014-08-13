package org.openmhealth.shim;

import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;

public interface OAuth2Shim {

    /**
     * Request parameters to be used when 'triggering'
     * spring oauth2. This should be the equivalent
     * of a ping to the external data provider.
     *
     * @return - The Shim data request to use for trigger.
     */
    public abstract ShimDataRequest getTriggerDataRequest();

    public abstract OAuth2ProtectedResourceDetails getResource();

    public abstract AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider();
}
