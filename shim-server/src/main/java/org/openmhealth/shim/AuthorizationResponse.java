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
 * @author Danilo Bonilla
 */
public class AuthorizationResponse {

    public enum Type {AUTHORIZED, DENIED, ERROR}

    private Type type;

    private String details;

    private AccessParameters accessParameters;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public AccessParameters getAccessParameters() {
        return accessParameters;
    }

    public void setAccessParameters(AccessParameters accessParameters) {
        this.accessParameters = accessParameters;
    }

    public static AuthorizationResponse error(String error) {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setType(Type.ERROR);
        response.setDetails(error);
        return response;
    }

    public static AuthorizationResponse denied() {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setType(Type.DENIED);
        return response;
    }

    public static AuthorizationResponse denied(String details) {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setType(Type.DENIED);
        response.details = details;
        return response;
    }

    public static AuthorizationResponse authorized() {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setType(Type.AUTHORIZED);
        return response;
    }

    public static AuthorizationResponse authorized(AccessParameters accessParameters) {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setAccessParameters(accessParameters);
        response.setType(Type.AUTHORIZED);
        return response;
    }
}
