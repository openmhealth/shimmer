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
import org.junit.Ignore;
import org.junit.Test;
import org.openmhealth.schema.pojos.Activity;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Danilo Bonilla
 */
public class ActivityBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    // fixme: remove the ignore once the test is fixed
    @Ignore("requires changes to Activity and ActivityBuilder to pass")
    public void testParse() throws IOException, ProcessingException {

        final String PHYSICAL_ACTIVITY_SCHEMA = "http://www.openmhealth.org/schema/omh/clinical/physical-activity-1.0.json";

        ObjectMapper mapper = new ObjectMapper();

        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(PHYSICAL_ACTIVITY_SCHEMA);

        ProcessingReport report;

        ActivityBuilder builder = new ActivityBuilder();

        Activity invalidActivity = builder.build();
        String invalidJson = mapper.writeValueAsString(invalidActivity);

        report = schema.validate(mapper.readTree(invalidJson));
        System.out.println(report);
        assertFalse("Expected invalid result but got success", report.isSuccess());

        Activity activity = builder
                .withStartAndDuration(
                        new DateTime(), 3100d, DurationUnitValue.DurationUnit.sec)
                .setReportedActivityIntensity(Activity.ActivityIntensity.moderate)
                .setActivityName("snow boarding")
                .setDistance(5d, LengthUnitValue.LengthUnit.mi).build();

        String rawJson = mapper.writeValueAsString(activity);

        Activity deserialized = mapper.readValue(rawJson, Activity.class);

        assertNotNull(deserialized.getEffectiveTimeFrame().getTimeInterval().getDateTime());
        assertNotNull(deserialized.getEffectiveTimeFrame().getTimeInterval().getDuration());
        assertNotNull(deserialized.getActivityName());
        assertNotNull(deserialized.getDistance().getUnit());
        assertNotNull(deserialized.getDistance().getValue());
        assertNotNull(deserialized.getReportedActivityIntensity());

        report = schema.validate(mapper.readTree(rawJson));
        System.out.println(report);

        assertTrue("Expected valid result!", report.isSuccess());
    }

}
