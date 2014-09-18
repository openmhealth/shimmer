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

package org.openmhealth.shim.runkeeper;

import org.openmhealth.shim.ApplicationAccessParameters;
import org.openmhealth.shim.ApplicationAccessParametersRepo;
import org.openmhealth.shim.ShimConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Danilo Bonilla
 */
@Component
@ConfigurationProperties(prefix = "openmhealth.shim.runkeeper")
public class RunkeeperConfig implements ShimConfig {

    private String clientId;

    private String clientSecret;

    @Autowired
    private ApplicationAccessParametersRepo applicationParametersRepo;

    public String getClientId() {
        ApplicationAccessParameters parameters =
            applicationParametersRepo.findByShimKey(RunkeeperShim.SHIM_KEY);
        return parameters.getClientId() != null ? parameters.getClientId() : clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        ApplicationAccessParameters parameters =
            applicationParametersRepo.findByShimKey(RunkeeperShim.SHIM_KEY);
        return parameters.getClientSecret() != null ? parameters.getClientSecret() : clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
