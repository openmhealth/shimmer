/*
 * Copyright 2015 Open mHealth
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

package org.openmhealth.shim.ihealth.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyMassIndex1;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.TypedUnitValue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.BodyMassIndex1.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.BodyMassIndexUnit1.KILOGRAMS_PER_SQUARE_METER;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBodyMassIndexDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private final IHealthBodyMassIndexDataPointMapper mapper = new IHealthBodyMassIndexDataPointMapper();
    private List<DataPoint<BodyMassIndex1>> dataPoints;

    @BeforeTest
    public void initializeResponse() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-weight.json");
    }

    @BeforeMethod
    public void initializeDataPoints() {

        dataPoints = mapper.asDataPoints(responseNode);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        BodyMassIndex1 expectedBodyMassIndex = new BodyMassIndex1.Builder(new TypedUnitValue<>(
                KILOGRAMS_PER_SQUARE_METER, 22.56052563257619))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T12:04:09-08:00"))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBodyMassIndex));

        testDataPointHeader(dataPoints.get(0).getHeader(), SCHEMA_ID, SENSED, "5fe5893c418b48cd8da7954f8b6c2f36",
                OffsetDateTime.parse("2015-09-17T20:04:17Z"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        BodyMassIndex1 expectedBodyMassIndex = new BodyMassIndex1.Builder(
                new TypedUnitValue<>(KILOGRAMS_PER_SQUARE_METER, 22.56052398681641))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T14:07:57-06:00"))
                .setUserNotes("Weight so good, look at me now")
                .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBodyMassIndex));

        testDataPointHeader(dataPoints.get(1).getHeader(), SCHEMA_ID, SELF_REPORTED,
                "b702a3a5e998f2fca268df6daaa69871", OffsetDateTime.parse("2015-09-17T20:08:00Z"));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenBodyMassIndexValueIsZero() throws IOException {

        JsonNode zeroValueNode =
                asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-weight-no-weight-value.json");

        assertThat(mapper.asDataPoints(zeroValueNode), is(empty()));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenWeightListIsEmpty() throws IOException {

        JsonNode emptyListNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-weight-empty.json");

        assertThat(mapper.asDataPoints(emptyListNode), is(empty()));
    }

}
