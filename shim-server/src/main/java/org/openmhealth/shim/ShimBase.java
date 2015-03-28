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
    public String getClientId() {
        ApplicationAccessParameters parameters = findParameters();
        return parameters != null ? parameters.getClientId() : clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        ApplicationAccessParameters parameters = findParameters();
        return parameters != null ? parameters.getClientSecret() : clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    private ApplicationAccessParameters findParameters() {
        return applicationParametersRepo.findByShimKey(getShimKey());
    }

    @Override
    public boolean isConfigured() {
        return getClientId() != null && getClientSecret() != null;
    }
}
