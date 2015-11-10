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

import com.google.common.collect.Maps;
import org.openmhealth.shimmer.common.domain.parameters.RequestParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * A builder that constructs a {@link RequestEntity}.
 *
 * @author Emerson Farrugia
 * @author Chris Schaefbauer
 */
public class RequestEntityBuilder<T> {


    private UriTemplate uriTemplate;
    private HttpMethod httpMethod = HttpMethod.GET;
    private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    private MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
    private Map<String, String> pathParameters = Maps.newHashMap();
    private boolean finishedAssembling;


    /**
     * @param uriTemplate the request entity URI as a template
     */
    public RequestEntityBuilder(UriTemplate uriTemplate) {

        checkNotNull(uriTemplate);

        this.uriTemplate = uriTemplate;
    }

    /**
     * @return the request entity URI as a template
     */
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    /**
     * @param uriTemplate URI template for the builder to use in constructing the request entity
     */
    public void setUriTemplate(UriTemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    /**
     * @return the method to use to exchange the request entity
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {

        checkNotNull(httpMethod);

        this.httpMethod = httpMethod;
    }

    /**
     * @param name the name of the header to add
     * @param value the value of the header
     */
    public void addHeader(String name, String value) {

        addToMultiValueMap(headers, name, value);
    }

    /**
     * @param name the name of the query parameter to add
     * @param value the value of the parameter
     */
    public void addQueryParameter(String name, String value) {

        addToMultiValueMap(queryParameters, name, value);
    }

    public void addPathParameter(String name, String value) {

        // Do we need to be able to handle multivalue and then figure out delineation or do we just require the value
        // to contain all the values separated properly?
        checkNotNull(name);
        checkArgument(!name.isEmpty());
        checkNotNull(value);
        checkArgument(!value.isEmpty());
        pathParameters.put(name, value);
    }

    private void addToMultiValueMap(MultiValueMap<String, String> map, String key, String value) {

        checkNotNull(key);
        checkArgument(!key.isEmpty());
        checkNotNull(value);
        checkArgument(!value.isEmpty());

        map.add(key, value);
    }

    /**
     * @return the request entity
     */
    public RequestEntity<T> build() {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uriTemplate.toString())
                .queryParams(queryParameters);

        String completedUriString = uriBuilder.buildAndExpand(pathParameters).toString();

        try {
            completedUriString = URLDecoder.decode(completedUriString, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {

            // In this case we likely don't have a URI that needs decoding, so we can continue with the uri that was
            // generated before decoding.
            e.printStackTrace();
        }
        uriBuilder = UriComponentsBuilder.fromUriString(completedUriString);

        return new RequestEntity(headers, httpMethod, uriBuilder.build().encode().toUri());
    }

    public boolean isFinishedAssembling() {
        return finishedAssembling;
    }

    public void setFinishedAssembling(boolean finishedAssembling) {
        this.finishedAssembling = finishedAssembling;
    }

    public void addParameterWithValue(RequestParameter parameter, String value) {

        checkNotNull(parameter);
        checkNotNull(value);
        checkArgument(!value.isEmpty());

        String parameterName = parameter.getParameterName();
        switch ( parameter.getRequestParameterLocation() ) {

            case QUERY:
                addQueryParameter(parameterName, value);
                break;
            case PATH:
                addPathParameter(parameterName, value);
                break;
            case HEADER:
                addHeader(parameterName, value);
                break;
        }
    }
}
