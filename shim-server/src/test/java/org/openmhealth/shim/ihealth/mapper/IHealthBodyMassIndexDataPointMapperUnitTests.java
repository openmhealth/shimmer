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
import org.openmhealth.schema.domain.omh.*;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.BodyMassIndex.*;
import static org.openmhealth.schema.domain.omh.DataPointModality.*;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBodyMassIndexDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private IHealthBodyMassIndexDataPointMapper mapper = new IHealthBodyMassIndexDataPointMapper();

    @BeforeTest
    public void initializeResponse() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/ihealth/mapper/ihealth-body-weight.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyMassIndex.Builder expectedBodyMassIndexBuilder = new BodyMassIndex.Builder(new TypedUnitValue<>(
                BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER, 22.56052563257619))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T12:04:09-08:00"));

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBodyMassIndexBuilder.build()));

        testDataPointHeader(dataPoints.get(0).getHeader(), SCHEMA_ID, SENSED, "5fe5893c418b48cd8da7954f8b6c2f36",
                OffsetDateTime.parse("2015-09-17T20:04:17Z"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyMassIndex.Builder expectedBodyMassIndexBuilder = new BodyMassIndex.Builder(
                new TypedUnitValue<>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER, 22.56052398681641))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T14:07:57-06:00"))
                .setUserNotes("Weight so good, look at me now");

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBodyMassIndexBuilder.build()));

        testDataPointHeader(dataPoints.get(1).getHeader(), SCHEMA_ID, SELF_REPORTED,
                "b702a3a5e998f2fca268df6daaa69871", OffsetDateTime.parse("2015-09-17T20:08:00Z"));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenBodyMassIndexValueIsZero() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/ihealth/mapper/ihealth-missing-body-weight-value.json");
        JsonNode zeroValueNode = objectMapper.readTree(resource.getInputStream());

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(zeroValueNode));
        assertThat(dataPoints.size(), equalTo(0));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenWeightListIsEmpty() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/ihealth/mapper/ihealth-empty-body-weight.json");
        JsonNode emptyListNode = objectMapper.readTree(resource.getInputStream());

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(emptyListNode));
        assertThat(dataPoints.size(), equalTo(0));
    }

}
