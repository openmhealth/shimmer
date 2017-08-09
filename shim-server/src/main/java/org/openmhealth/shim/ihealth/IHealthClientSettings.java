/*
 * Copyright 2017 Open mHealth
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
 *
 */

package org.openmhealth.shim.ihealth;

import org.hibernate.validator.constraints.URL;
import org.openmhealth.shim.OAuth2ClientSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;


/**
 * @author Emerson Farrugia
 */
@Component
@ConfigurationProperties("openmhealth.shim.ihealth")
public class IHealthClientSettings extends OAuth2ClientSettings {

    private boolean sandboxed = false;
    private String clientSerialNumber;
    private String activityEndpointSecret;
    private String bloodGlucoseEndpointSecret;
    private String bloodPressureEndpointSecret;
    private String sleepEndpointSecret;
    private String spO2EndpointSecret;
    private String sportEndpointSecret;
    private String weightEndpointSecret;


    public boolean isSandboxed() {

        return sandboxed;
    }

    public void setSandboxed(boolean sandboxed) {

        this.sandboxed = sandboxed;
    }

    @Override
    public List<String> getScopes() {

        List<String> scopeApiValues = new ArrayList<>();

        for (IHealthScope scope : IHealthScope.values()) {

            if (sandboxed && !scope.isAvailableInSandbox()) {
                continue;
            }

            if (!isEndpointSecretConfigured(scope)) {
                continue;
            }

            scopeApiValues.add(scope.getApiValue());
        }

        return scopeApiValues;
    }

    private boolean isEndpointSecretConfigured(IHealthScope scope) {

        return getEndpointSecret(scope).isPresent();
    }

    private Optional<String> getEndpointSecret(IHealthScope scope) {

        switch (scope) {
            case ACTIVITY_REPORT:
                return ofNullable(getActivityEndpointSecret());

            case BLOOD_GLUCOSE:
                return ofNullable(getBloodGlucoseEndpointSecret());

            case BLOOD_PRESSURE:
                return ofNullable(getBloodPressureEndpointSecret());

            case SLEEP_REPORT:
                return ofNullable(getSleepEndpointSecret());

            case BLOOD_OXYGEN:
                return ofNullable(getSpO2EndpointSecret());

            case SPORT_REPORT:
                return ofNullable(getSportEndpointSecret());

            case WEIGHT:
                return ofNullable(getWeightEndpointSecret());
        }

        return empty();
    }

    @URL
    public String getApiBaseUrl() {

        return sandboxed
                ? "http://sandboxapi.ihealthlabs.com/OpenApiV2"
                : "https://api.ihealthlabs.com:8443/OpenApiV2";
    }

    public String getClientSerialNumber() {

        return clientSerialNumber;
    }

    public void setClientSerialNumber(String clientSerialNumber) {

        this.clientSerialNumber = clientSerialNumber;
    }

    public String getActivityEndpointSecret() {

        return activityEndpointSecret;
    }

    public void setActivityEndpointSecret(String activityEndpointSecret) {

        this.activityEndpointSecret = activityEndpointSecret;
    }

    public String getBloodGlucoseEndpointSecret() {

        return bloodGlucoseEndpointSecret;
    }

    public void setBloodGlucoseEndpointSecret(String bloodGlucoseEndpointSecret) {

        this.bloodGlucoseEndpointSecret = bloodGlucoseEndpointSecret;
    }

    public String getBloodPressureEndpointSecret() {

        return bloodPressureEndpointSecret;
    }

    public void setBloodPressureEndpointSecret(String bloodPressureEndpointSecret) {

        this.bloodPressureEndpointSecret = bloodPressureEndpointSecret;
    }

    public String getSleepEndpointSecret() {

        return sleepEndpointSecret;
    }

    public void setSleepEndpointSecret(String sleepEndpointSecret) {

        this.sleepEndpointSecret = sleepEndpointSecret;
    }

    public String getSpO2EndpointSecret() {

        return spO2EndpointSecret;
    }

    public void setSpO2EndpointSecret(String spO2EndpointSecret) {

        this.spO2EndpointSecret = spO2EndpointSecret;
    }

    public String getSportEndpointSecret() {

        return sportEndpointSecret;
    }

    public void setSportEndpointSecret(String sportEndpointSecret) {

        this.sportEndpointSecret = sportEndpointSecret;
    }

    public String getWeightEndpointSecret() {

        return weightEndpointSecret;
    }

    public void setWeightEndpointSecret(String weightEndpointSecret) {

        this.weightEndpointSecret = weightEndpointSecret;
    }
}
