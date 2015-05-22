package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.openmhealth.schema.serializer.Rfc3339OffsetDateTimeSerializer;
import org.testng.annotations.BeforeClass;

import java.time.OffsetDateTime;


/**
 * @author Emerson Farrugia
 */
public abstract class DataPointMapperUnitTests {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void initializeObjectMapper() {

        // we represent JSON numbers as Java BigDecimals
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        // we serialize dates, date times, and times as strings, not numbers
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // we default to the ISO8601 format for JSR-310 and support Optional
        objectMapper.registerModule(new JSR310Module());
        objectMapper.registerModule(new Jdk8Module());

        // but we have to explicitly support the RFC3339 format over ISO8601 to make JSON Schema happy, specifically to
        // prevent the truncation of zero second fields
        SimpleModule rfc3339Module = new SimpleModule("rfc3339Module");
        rfc3339Module.addSerializer(new Rfc3339OffsetDateTimeSerializer(OffsetDateTime.class));
        objectMapper.registerModule(rfc3339Module);
    }
}
