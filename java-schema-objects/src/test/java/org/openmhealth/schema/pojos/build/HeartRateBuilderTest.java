/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.schema.pojos.build;

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

import static org.junit.Assert.*;

/**
 * @author Danilo Bonilla
 */
public class HeartRateBuilderTest {

    @Test
    public void test() throws IOException, ProcessingException {

        final String HEART_RATE_SCHEMA = "http://www.openmhealth.org/schema/omh/heart-rate-1.0.json";

        ObjectMapper mapper = new ObjectMapper();

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(HEART_RATE_SCHEMA);

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
