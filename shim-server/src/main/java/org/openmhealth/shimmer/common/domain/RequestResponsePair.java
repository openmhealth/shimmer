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

package org.openmhealth.shimmer.common.domain;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;


/**
 * An HTTP request-response pair.
 *
 * @param <Q> the request body
 * @param <S> the response boxy
 * @author Emerson Farrugia
 */
public class RequestResponsePair<Q, S> implements Comparable<RequestResponsePair<Q, S>> {

    private RequestEntity<Q> requestEntity;
    private ResponseEntity<S> responseEntity;
    private OffsetDateTime requestDateTime;
    private OffsetDateTime responseDateTime;

    /**
     * @return the request that was sent
     */
    public RequestEntity<Q> getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity<Q> requestEntity) {
        this.requestEntity = requestEntity;
    }

    /**
     * @return the response that was received
     */
    public ResponseEntity<S> getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(ResponseEntity<S> responseEntity) {
        this.responseEntity = responseEntity;
    }

    /**
     * @return the timestamp at which the request was sent
     */
    public OffsetDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(OffsetDateTime requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    /**
     * @return the timestamp at which the response was received
     */
    public OffsetDateTime getResponseDateTime() {
        return responseDateTime;
    }

    public void setResponseDateTime(OffsetDateTime responseDateTime) {
        this.responseDateTime = responseDateTime;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        RequestResponsePair<?, ?> that = (RequestResponsePair<?, ?>) object;

        if (!requestEntity.equals(that.requestEntity)) {
            return false;
        }
        if (!responseEntity.equals(that.responseEntity)) {
            return false;
        }
        if (!requestDateTime.equals(that.requestDateTime)) {
            return false;
        }
        return responseDateTime.equals(that.responseDateTime);
    }

    @Override
    public int hashCode() {
        int result = requestEntity.hashCode();
        result = 31 * result + responseEntity.hashCode();
        result = 31 * result + requestDateTime.hashCode();
        result = 31 * result + responseDateTime.hashCode();
        return result;
    }

    @Override
    public int compareTo(RequestResponsePair<Q, S> that) {

        if (this.equals(that)) {
            return 0;
        }

        if (this.getRequestDateTime().isBefore(that.getRequestDateTime())) {
            return -1;
        }

        if (this.getRequestDateTime().isAfter(that.getRequestDateTime())) {
            return 1;
        }

        if (this.getResponseDateTime().isBefore(that.getResponseDateTime())) {
            return -1;
        }

        if (this.getResponseDateTime().isAfter(that.getResponseDateTime())) {
            return 1;
        }

        return 0;
    }
}
