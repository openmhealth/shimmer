package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.openmhealth.schema.configuration.JacksonConfiguration.newObjectMapper;


/**
 * @author Emerson Farrugia
 */
public abstract class DataPointMapperUnitTests {

    protected static final ObjectMapper objectMapper = newObjectMapper();
}
