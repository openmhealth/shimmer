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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.BloodGlucose.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBloodGlucoseDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private IHealthBloodGlucoseDataPointMapper mapper = new IHealthBloodGlucoseDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/ihealth/mapper/ihealth-blood-glucose.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<BloodGlucose>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        List<DataPoint<BloodGlucose>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BloodGlucose.Builder expectedBloodGlucoseBuilder = new BloodGlucose.Builder(
                new TypedUnitValue<>(BloodGlucoseUnit.MILLIGRAMS_PER_DECILITER, 60))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T12:03:27-08:00"))
                .setTemporalRelationshipToMeal(TemporalRelationshipToMeal.BEFORE_BREAKFAST)
                .setUserNotes("Such glucose, much blood.");

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBloodGlucoseBuilder.build()));

        assertThat(dataPoints.get(0).getBody().getAdditionalProperty("temporal_relationship_to_medication").get(),
                equalTo("Before_taking_pills"));

        testDataPointHeader(dataPoints.get(0).getHeader(), SCHEMA_ID, SENSED,
                "f706b6152f684c0e9185b1fa6b7c5148", OffsetDateTime.parse("2015-09-17T20:03:41Z"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        List<DataPoint<BloodGlucose>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BloodGlucose.Builder expectedBloodGlucoseBuilder =
                new BloodGlucose.Builder(new TypedUnitValue<>(BloodGlucoseUnit.MILLIGRAMS_PER_DECILITER, 70))
                        .setTemporalRelationshipToMeal(TemporalRelationshipToMeal.AFTER_BREAKFAST)
                        .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-24T14:44:40-06:00"));

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBloodGlucoseBuilder.build()));

        assertThat(dataPoints.get(1).getBody().getAdditionalProperty("temporal_relationship_to_medication").get(),
                equalTo("After_taking_pills"));

        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenBloodGlucoseListIsEmpty() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/ihealth/mapper/ihealth-empty-glucose.json");
        JsonNode emptyListResponseNode = objectMapper.readTree(resource.getInputStream());

        assertThat(mapper.asDataPoints(singletonList(emptyListResponseNode)), is(empty()));
    }

    @Test
    public void getBloodGlucoseUnitFromMagicNumberShouldReturnCorrectBloodGlucoseUnit() {

        assertThat(mapper.getBloodGlucoseUnitFromMagicNumber(0), equalTo(BloodGlucoseUnit.MILLIGRAMS_PER_DECILITER));
        assertThat(mapper.getBloodGlucoseUnitFromMagicNumber(1), equalTo(BloodGlucoseUnit.MILLIMOLES_PER_LITER));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void iHealthBloodGlucoseUnitEnumShouldThrowExceptionWhenInvalidMagicNumber() {

        mapper.getBloodGlucoseUnitFromMagicNumber(5);
    }

}
