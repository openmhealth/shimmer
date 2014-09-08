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
