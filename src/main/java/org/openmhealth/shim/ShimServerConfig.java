/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Danilo Bonilla
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.server")
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