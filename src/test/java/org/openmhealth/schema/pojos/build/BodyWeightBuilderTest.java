/*
 * Copyright 2014 Open mHealth
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

package org.openmhealth.schema.pojos.build;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openmhealth.schema.pojos.BodyWeight;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;

import static org.junit.Assert.*;
import static org.openmhealth.schema.pojos.generic.MassUnitValue.MassUnit.lb;

/**
 * Ensures that the body weight builder class
 * generates JSON that conforms to the body weight clinical schema.
 *
 * @author Danilo Bonilla
 */
public class BodyWeightBuilderTest {

    @Test
    public void test() throws IOException, ProcessingException {

        final String BODY_WEIGHT_SCHEMA = "http://www.openmhealth.org/schema/omh/clinical/body-weight-1.0.json";

        ObjectMapper mapper = new ObjectMapper();

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(BODY_WEIGHT_SCHEMA);

        ProcessingReport report;

        BodyWeightBuilder builder = new BodyWeightBuilder();

        BodyWeight invalidBodyWeight = builder.build();
        String invalidJson = mapper.writeValueAsString(invalidBodyWeight);

        report = schema.validate(mapper.readTree(invalidJson));
        System.out.println(report);
        assertFalse("Expected invalid result but got success", report.isSuccess());

        builder.setWeight(230d, lb);
        builder.setTimeTaken(new DateTime());
        BodyWeight bodyWeight = builder.build();

        assertNotNull(bodyWeight.getEffectiveTimeFrame());
        assertNotNull(bodyWeight.getMassUnitValue());
        assertEquals(bodyWeight.getMassUnitValue().getUnit(),lb);
        assertEquals(bodyWeight.getMassUnitValue().getValue(),new BigDecimal(230d));

        String rawJson = mapper.writeValueAsString(bodyWeight);

        report = schema.validate(mapper.readTree(rawJson));
        System.out.println(report);

        assertTrue("Expected valid result!", report.isSuccess());
    }
}
