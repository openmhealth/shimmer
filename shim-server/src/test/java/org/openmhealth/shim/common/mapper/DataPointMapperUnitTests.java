package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static org.openmhealth.schema.configuration.JacksonConfiguration.newObjectMapper;


/**
 * @author Emerson Farrugia
 */
public abstract class DataPointMapperUnitTests {

    protected static final ObjectMapper objectMapper = newObjectMapper();


    /**
     * @param classPathResourceName the name of the class path resource to load
     * @return the contents of the resource as a {@link JsonNode}
     * @throws RuntimeException if the resource can't be loaded
     */
    protected JsonNode asJsonNode(String classPathResourceName) {

        ClassPathResource resource = new ClassPathResource(classPathResourceName);

        try {
            InputStream resourceInputStream = resource.getInputStream();
            return objectMapper.readTree(resourceInputStream);
        }
        catch (IOException e) {
            throw new RuntimeException(
                    format("The class path resource '%s' can't be loaded as a JSON node.", classPathResourceName), e);
        }
    }
}
