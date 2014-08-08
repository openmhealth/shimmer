package org.openmhealth.shim;


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
