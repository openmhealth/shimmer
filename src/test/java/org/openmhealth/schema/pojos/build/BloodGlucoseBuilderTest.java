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
import org.openmhealth.schema.pojos.BloodGlucose;
import org.openmhealth.schema.pojos.BloodPressure;
import org.openmhealth.schema.pojos.BloodSpecimenType;
import org.openmhealth.schema.pojos.PositionDuringMeasurement;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BloodGlucoseBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException, ProcessingException {
        final String BLOOD_GLUCOSE_SCHEMA = "schemas/blood-glucose-1.0.json";

        URL url = Thread.currentThread().getContextClassLoader().getResource(BLOOD_GLUCOSE_SCHEMA);
        assertNotNull(url);

        ObjectMapper mapper = new ObjectMapper();

        InputStream inputStream = url.openStream();
        JsonNode schemaNode = mapper.readTree(inputStream);

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(schemaNode);

        ProcessingReport report;

        BloodGlucoseBuilder builder = new BloodGlucoseBuilder();

        BloodGlucose invalidBloodGlucose = builder.build();
        String invalidJson = mapper.writeValueAsString(invalidBloodGlucose);

        report = schema.validate(mapper.readTree(invalidJson));
        System.out.println(report);
        assertFalse("Expected invalid result but got success", report.isSuccess());

        BloodGlucose bloodGlucose = builder
            .setBloodSpecimenType(BloodSpecimenType.whole_blood)
            .setMgdLValue(new BigDecimal(90d))
            .setNotes("yo yo yo")
            .setTimeTaken(new DateTime())
            .setDescriptiveStatistic(DescriptiveStatistic.average).build();

        String rawJson = mapper.writeValueAsString(bloodGlucose);

        BloodGlucose deserialized = mapper.readValue(rawJson, BloodGlucose.class);

        assertNotNull(deserialized.getEffectiveTimeFrame());
        assertNotNull(deserialized.getBloodGlucose().getUnit());
        assertNotNull(deserialized.getBloodGlucose().getValue());
        assertNotNull(deserialized.getNotes());
        assertNotNull(deserialized.getDescriptiveStatistic());

        report = schema.validate(mapper.readTree(rawJson));
        System.out.println(report);

        assertTrue("Expected valid result!", report.isSuccess());
    }
}
