/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim;

/**
 * Base class for shims.
 *
 * @author Eric Jain
 */
public abstract class ShimBase implements Shim {

    private String clientId;

    private String clientSecret;

    private final ApplicationAccessParametersRepo applicationParametersRepo;

    protected ShimBase(ApplicationAccessParametersRepo applicationParametersRepo) {
        this.applicationParametersRepo = applicationParametersRepo;
    }

    @Override
    public ApplicationAccessParameters findApplicationAccessParameters() {
        ApplicationAccessParameters parameters = applicationParametersRepo.findByShimKey(getShimKey());
        if (parameters == null) {
            parameters = new ApplicationAccessParameters(getShimKey(), clientId, clientSecret);
        }
        return parameters;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public boolean isConfigured() {
        ApplicationAccessParameters parameters = findApplicationAccessParameters();
        return parameters.getClientId() != null && parameters.getClientSecret() != null;
    }
}
