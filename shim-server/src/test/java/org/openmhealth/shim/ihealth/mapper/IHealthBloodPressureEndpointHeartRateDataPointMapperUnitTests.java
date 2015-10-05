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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBloodPressureEndpointHeartRateDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    JsonNode responseNode;

    private IHealthBloodPressureEndpointHeartRateDataPointMapper mapper =
            new IHealthBloodPressureEndpointHeartRateDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/ihealth/mapper/ihealth-blood-pressure.json");
        responseNode = objectMapper.readTree(resource.getInputStream());

    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        HeartRate.Builder expectedHeartRateBuilder = new HeartRate.Builder(100)
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T12:04:23-08:00"));
        HeartRate expectedSensedHeartRate = expectedHeartRateBuilder.build();
        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSensedHeartRate));

        testDataPointHeader(dataPoints.get(0).getHeader(), HeartRate.SCHEMA_ID, SENSED,
                "c62b84d9d4b7480a8ff2aef1465aa454", OffsetDateTime.parse("2015-09-17T20:04:30Z"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        HeartRate expectedHeartRate = new HeartRate.Builder(75)
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T14:07:45-06:00"))
                .setUserNotes("BP on the up and up.")
                .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedHeartRate));
        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnCorrectUserNotesWithDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints.get(0).getBody().getUserNotes(), nullValue());
        assertThat(dataPoints.get(1).getBody().getUserNotes(), equalTo("BP on the up and up."));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointWhenHeartRateDataIsNotPresent() throws IOException {

        ClassPathResource resource = new ClassPathResource(
                "org/openmhealth/shim/ihealth/mapper/ihealth-blood-pressure-missing-heart-rate.json");
        JsonNode noHeartRateBloodPressureNode = objectMapper.readTree(resource.getInputStream());

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(noHeartRateBloodPressureNode));
        assertThat(dataPoints.size(), equalTo(0));

    }
}
