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

import static java.time.OffsetDateTime.now;


/**
 * Wrapper for responses received from shims.
 * <p/>
 * todo: expand to include original parameters TODO there's no pagination information, how does a caller know how to
 * proceed?
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

    // TODO in seconds since the epoch? needs to be documented
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

    public static ShimDataResponse empty(String shimKey) {

        ShimDataResponse response = new ShimDataResponse();

        response.setShim(shimKey);
        response.setBody(null);
        response.setTimeStamp(now().toEpochSecond());

        return response;
    }

    public static ShimDataResponse result(String shimKey, Object object) {

        ShimDataResponse response = new ShimDataResponse();

        response.setShim(shimKey);
        response.setBody(object);
        response.setTimeStamp(now().toEpochSecond());

        return response;
    }
}
