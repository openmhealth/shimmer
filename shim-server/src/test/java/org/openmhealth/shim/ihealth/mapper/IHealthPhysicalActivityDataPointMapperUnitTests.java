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
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.PhysicalActivity.SCHEMA_ID;


/**
 * @author Chris Schaefbauer
 */
public class IHealthPhysicalActivityDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {


    private JsonNode responseNode;
    private IHealthPhysicalActivityDataPointMapper mapper = new IHealthPhysicalActivityDataPointMapper();
    private List<DataPoint<PhysicalActivity>> dataPoints;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("/org/openmhealth/shim/ihealth/mapper/ihealth-sport.json");
    }

    @BeforeMethod
    public void initializeDataPoints() {

        dataPoints = mapper.asDataPoints(responseNode);
    }

    @Test
    public void asDataPointsShouldReturnTheCorrectNumberOfDataPoints() {

        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        PhysicalActivity.Builder expectedPhysicalActivityBuilder =
                new PhysicalActivity.Builder("Swimming, breaststroke")
                        .setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                                OffsetDateTime.parse("2015-09-17T20:02:28-08:00"),
                                OffsetDateTime.parse("2015-09-17T20:32:28-08:00")))
                        .setCaloriesBurned(new KcalUnitValue(KILOCALORIE, 221.5));

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedPhysicalActivityBuilder.build()));

        testDataPointHeader(dataPoints.get(0).getHeader(), SCHEMA_ID, SENSED,
                "3f8770f51cc84957a57d20f4fee1f34b", OffsetDateTime.parse("2015-09-17T20:02:57Z"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        PhysicalActivity.Builder expectedPhysicalActivityBuilder = new PhysicalActivity.Builder("Running")
                .setEffectiveTimeFrame(
                        TimeInterval.ofStartDateTimeAndEndDateTime(
                                OffsetDateTime.parse("2015-09-22T20:43:03+01:00"),
                                OffsetDateTime.parse("2015-09-22T21:13:03+01:00")))
                .setCaloriesBurned(new KcalUnitValue(KILOCALORIE, 202.5));

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedPhysicalActivityBuilder.build()));

        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsReturnsNoDataPointsForAnEmptyList() throws IOException {

        JsonNode emptyListResponseNode =
                asJsonNode("/org/openmhealth/shim/ihealth/mapper/ihealth-sport-empty.json");

        assertThat(mapper.asDataPoints(emptyListResponseNode), is(empty()));
    }

}
