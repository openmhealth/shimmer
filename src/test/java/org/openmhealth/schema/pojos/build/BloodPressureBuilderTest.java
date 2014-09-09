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
import org.openmhealth.schema.pojos.BloodPressure;
import org.openmhealth.schema.pojos.PositionDuringMeasurement;
import org.openmhealth.schema.pojos.generic.DescriptiveStatistic;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Danilo Bonilla
 */
public class BloodPressureBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException, ProcessingException {

        final String BLOOD_PRESSURE_SCHEMA = "http://www.openmhealth.org/schema/omh/clinical/blood-pressure-1.0.json";

        ObjectMapper mapper = new ObjectMapper();

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(BLOOD_PRESSURE_SCHEMA);

        ProcessingReport report;

        BloodPressureBuilder builder = new BloodPressureBuilder();

        BloodPressure invalidBloodPressure = builder.build();
        String invalidJson = mapper.writeValueAsString(invalidBloodPressure);

        report = schema.validate(mapper.readTree(invalidJson));
        System.out.println(report);
        assertFalse("Expected invalid result but got success", report.isSuccess());

        BloodPressure bloodPressure = builder
            .setValues(new BigDecimal(120d), new BigDecimal(80d))
            .setNotes("yo yo yo")
            .setTimeTaken(new DateTime())
            .setPositionDuringMeasurement(PositionDuringMeasurement.lying_down)
            .setDescriptiveStatistic(DescriptiveStatistic.average).build();

        String rawJson = mapper.writeValueAsString(bloodPressure);

        BloodPressure deserialized = mapper.readValue(rawJson, BloodPressure.class);

        assertNotNull(deserialized.getEffectiveTimeFrame());
        assertNotNull(deserialized.getDiastolic());
        assertNotNull(deserialized.getSystolic());
        assertNotNull(deserialized.getNotes());
        assertNotNull(deserialized.getPositionDuringMeasurement());
        assertNotNull(deserialized.getDescriptiveStatistic());

        report = schema.validate(mapper.readTree(rawJson));
        System.out.println(report);

        assertTrue("Expected valid result!", report.isSuccess());
    }

}
