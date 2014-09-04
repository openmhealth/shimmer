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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openmhealth.schema.pojos.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openmhealth.schema.pojos.SleepDurationUnitValue.*;
import static org.openmhealth.schema.pojos.SleepDurationUnitValue.Unit.*;

public class SleepBuilderTest {

    @Test
    public void test() throws IOException, ProcessingException {
        final String SLEEP_DURATION_SCHEMA = "schemas/sleep-duration-1.0.json";

        URL url = Thread.currentThread().getContextClassLoader().getResource(SLEEP_DURATION_SCHEMA);
        assertNotNull(url);

        ObjectMapper mapper = new ObjectMapper();

        InputStream inputStream = url.openStream();
        JsonNode schemaNode = mapper.readTree(inputStream);

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(schemaNode);

        ProcessingReport report;

        SleepDurationBuilder builder = new SleepDurationBuilder();

        SleepDuration invalidSleepDuration = builder.build();
        String invalidJson = mapper.writeValueAsString(invalidSleepDuration);

        report = schema.validate(mapper.readTree(invalidJson));
        System.out.println(report);
        assertFalse("Expected invalid result but got success", report.isSuccess());

        SleepDuration sleepDuration = new SleepDurationBuilder()
            .withStartAndEndAndDuration(
                new DateTime(), new DateTime().plusMinutes(250), 3d, min)
            .setNotes("Tossed and turned")
            .build();

        String rawJson = mapper.writeValueAsString(sleepDuration);

        SleepDuration deserialized = mapper.readValue(rawJson, SleepDuration.class);

        assertNotNull(deserialized.getEffectiveTime().getDateTime());
        assertNotNull(deserialized.getEffectiveTime().getEndTime());
        assertNotNull(deserialized.getSleepDurationUnitValue().getUnit());
        assertNotNull(deserialized.getSleepDurationUnitValue().getValue());

        report = schema.validate(mapper.readTree(rawJson));
        System.out.println(report);

        assertTrue("Expected valid result!", report.isSuccess());
    }

}
