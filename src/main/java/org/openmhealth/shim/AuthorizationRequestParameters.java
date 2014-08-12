package org.openmhealth.shim;

import org.springframework.data.annotation.Id;
import org.springframework.http.HttpMethod;

import java.util.Map;

public class AuthorizationRequestParameters {

    @Id
    private String id;

    private String stateKey;

    private String username;

    private HttpMethod httpMethod = HttpMethod.POST;

    private String redirectUri;

    private Map<String, String> requestParams;

    private String authorizationUrl;

    private boolean isAuthorized = false;

    private byte[] serializedRequest;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getIsAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Map<String, String> requestParams) {
        this.requestParams = requestParams;
    }

    public byte[] getSerializedRequest() {
        return serializedRequest;
    }

    public void setSerializedRequest(byte[] serializedRequest) {
        this.serializedRequest = serializedRequest;
    }

    public static AuthorizationRequestParameters authorized() {
        AuthorizationRequestParameters params = new AuthorizationRequestParameters();
        params.setAuthorized(true);
        return params;
    }
}
