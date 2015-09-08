/*
 * Copyright 2014 Open mHealth
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


import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstraction for handling access parameters.
 *
 * @author Danilo Bonilla
 */
public class AccessParameters {

    private String id;

    private String username;

    private String shimKey;

    private String clientId;

    private String clientSecret;

    private String accessToken;

    private String tokenSecret;

    private String stateKey;

    private LocalDateTime dateCreated = LocalDateTime.now();

    private byte[] serializedToken; //Required only by spring oauth2

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShimKey() {
        return shimKey;
    }

    public void setShimKey(String shimKey) {
        this.shimKey = shimKey;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private Map<String, Object> additionalParameters = new LinkedHashMap<String, Object>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(Map<String, Object> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public byte[] getSerializedToken() {
        return serializedToken;
    }

    public void setSerializedToken(byte[] serializedToken) {
        this.serializedToken = serializedToken;
    }
}
