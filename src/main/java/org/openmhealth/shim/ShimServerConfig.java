package org.openmhealth.shim;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "org.openmhealth.shim.server")
public class ShimServerConfig {

    private String callbackUrlBase = "http://localhost:8083";

    public String getCallbackUrlBase() {
        return callbackUrlBase;
    }

    public void setCallbackUrlBase(String callbackUrlBase) {
        this.callbackUrlBase = callbackUrlBase;
    }

    public String getCallbackUrl(String shimKey, String stateKey) {
        return getCallbackUrl(shimKey) + "?state=" + stateKey;
    }

    public String getCallbackUrl(String shimKey) {
        return callbackUrlBase + "/authorize/" + shimKey + "/callback";
    }
}