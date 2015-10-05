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
import org.openmhealth.schema.domain.omh.PhysicalActivity;
import org.openmhealth.schema.domain.omh.TimeInterval;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.PhysicalActivity.SCHEMA_ID;


/**
 * @author Chris Schaefbauer
 */
public class IHealthPhysicalActivityDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {


    private JsonNode responseNode;
    private IHealthPhysicalActivityDataPointMapper mapper = new IHealthPhysicalActivityDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource = new ClassPathResource("/org/openmhealth/shim/ihealth/mapper/ihealth-sports-activity.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnTheCorrectNumberOfDataPoints(){

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints(){

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        PhysicalActivity.Builder expectedPhysicalActivityBuilder = new PhysicalActivity.Builder("Swimming, breaststroke")
                .setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2015-09-17T20:02:28-08:00"),
                        OffsetDateTime.parse("2015-09-17T20:32:28-08:00")));
        assertThat(dataPoints.get(0).getBody(),equalTo(expectedPhysicalActivityBuilder.build()));

        testDataPointHeader(dataPoints.get(0).getHeader(), SCHEMA_ID, SENSED,
                "3f8770f51cc84957a57d20f4fee1f34b", OffsetDateTime.parse("2015-09-17T20:02:57Z"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints(){

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));


        PhysicalActivity.Builder expectedPhysicalActivityBuilder = new PhysicalActivity.Builder("Running")
                .setEffectiveTimeFrame(
                        TimeInterval.ofStartDateTimeAndEndDateTime(
                                OffsetDateTime.parse("2015-09-22T20:43:03-06:00"),
                                OffsetDateTime.parse("2015-09-22T21:13:03-06:00")));

        assertThat(dataPoints.get(1).getBody(),equalTo(expectedPhysicalActivityBuilder.build()));

        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsReturnsNoDataPointsForAnEmptyList() throws IOException {

        ClassPathResource resource = new ClassPathResource("/org/openmhealth/shim/ihealth/mapper/ihealth-sports-activity-empty-list.json");
        JsonNode emptyListResponseNode = objectMapper.readTree(resource.getInputStream());

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(emptyListResponseNode));
        assertThat(dataPoints.size(),equalTo(0));
    }

}
