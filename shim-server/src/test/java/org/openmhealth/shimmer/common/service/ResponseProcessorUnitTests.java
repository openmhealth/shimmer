/*
 * Copyright 2016 Open mHealth
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

package org.openmhealth.shimmer.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static org.openmhealth.schema.configuration.JacksonConfiguration.newObjectMapper;


/**
 * @author Chris Schaefbauer
 */
public class ResponseProcessorUnitTests {

    /**
     * @param classPathResourceName the name of the class path resource to load
     * @return the contents of the resource as a {@link JsonNode}
     * @throws RuntimeException if the resource can't be loaded
     */
    protected JsonNode asJsonNode(String classPathResourceName) {

        ClassPathResource resource = new ClassPathResource(classPathResourceName);

        try {
            InputStream resourceInputStream = resource.getInputStream();
            return newObjectMapper().readTree(resourceInputStream);
        }
        catch (IOException e) {
            throw new RuntimeException(
                    format("The class path resource '%s' can't be loaded as a JSON node.", classPathResourceName), e);
        }
    }
}
