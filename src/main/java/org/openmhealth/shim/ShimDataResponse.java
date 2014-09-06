package org.openmhealth.shim;

import java.util.Calendar;

/**
 * Wrapper for responses received from shims.
 * <p/>
 * todo: expand to include original parameters
 *
 * @author Danilo Bonilla
 */
public class ShimDataResponse {

    private String shim;

    private Long timeStamp;

    private Object body;

    public String getShim() {
        return shim;
    }

    public void setShim(String shim) {
        this.shim = shim;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public static ShimDataResponse empty() {
        ShimDataResponse response = new ShimDataResponse();
        response.setBody(null);
        response.setTimeStamp(
            Calendar.getInstance().getTimeInMillis() / 1000);
        return response;
    }

    public static ShimDataResponse result(Object object) {
        ShimDataResponse response = new ShimDataResponse();
        response.setBody(object);
        response.setTimeStamp(
            Calendar.getInstance().getTimeInMillis() / 1000);
        return response;
    }
}
