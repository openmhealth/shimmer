package org.openmhealth.schema.pojos.build;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openmhealth.schema.pojos.HeartRate;
import org.openmhealth.schema.pojos.TemporalRelationshipToPhysicalActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HeartRateBuilderTest {

    @Test
    public void test() throws IOException, ProcessingException {
        final String HEART_RATE_SCHEMA = "schemas/heart-rate-1.0.json";

        URL url = Thread.currentThread().getContextClassLoader().getResource(HEART_RATE_SCHEMA);
        assertNotNull(url);

        ObjectMapper mapper = new ObjectMapper();

        InputStream inputStream = url.openStream();
        JsonNode schemaNode = mapper.readTree(inputStream);

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(schemaNode);

        ProcessingReport report;

        HeartRateBuilder builder = new HeartRateBuilder();

        HeartRate invalidHeartRate = builder.build();
        String invalidJson = mapper.writeValueAsString(invalidHeartRate);

        report = schema.validate(mapper.readTree(invalidJson));
        System.out.println(report);
        assertFalse("Expected invalid result but got success", report.isSuccess());

        builder.withRate(234);
        builder.withTimeTaken(new DateTime());
        builder.withTimeTakenDescription(
            TemporalRelationshipToPhysicalActivity.after_exercise);

        HeartRate heartRate = builder.build();

        assertNotNull(heartRate.getEffectiveTimeFrame());
        assertNotNull(heartRate.getHeartRate());

        String rawJson = mapper.writeValueAsString(heartRate);

        report = schema.validate(mapper.readTree(rawJson));
        System.out.println(report);

        assertTrue("Expected valid result!", report.isSuccess());
    }

}
