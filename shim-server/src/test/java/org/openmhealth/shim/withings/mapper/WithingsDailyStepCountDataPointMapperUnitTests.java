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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount2;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public class WithingsDailyStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    private WithingsDailyStepCountDataPointMapper mapper = new WithingsDailyStepCountDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("/org/openmhealth/shim/withings/mapper/withings-activity-measures.json");
    }

    @Test
    public void asDataPointsReturnsCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(singletonList(responseNode)).size(), equalTo(4));
    }

    @Test
    public void asDataPointsReturnsCorrectDataPoints() {

        List<DataPoint<StepCount2>> dataPoints = mapper.asDataPoints(responseNode);

        testDailyStepCountDataPoint(dataPoints.get(0), 2934, "2015-06-18T00:00:00-07:00", "2015-06-19T00:00:00-07:00");
        testDailyStepCountDataPoint(dataPoints.get(1), 2600, "2015-06-19T00:00:00-07:00", "2015-06-20T00:00:00-07:00");
        testDailyStepCountDataPoint(dataPoints.get(2), 5458, "2015-06-20T00:00:00-07:00", "2015-06-21T00:00:00-07:00");
        testDailyStepCountDataPoint(dataPoints.get(3), 1798, "2015-02-21T00:00:00-08:00", "2015-02-22T00:00:00-08:00");
    }

    public void testDailyStepCountDataPoint(DataPoint<StepCount2> stepCountDataPoint, long expectedStepCountValue,
            String expectedStartDateString, String expectedEndDateString) {

        StepCount2 expectedStepCount = new StepCount2.Builder(
                expectedStepCountValue,
                ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse(expectedStartDateString),
                        OffsetDateTime.parse(expectedEndDateString)))
                .build();

        StepCount2 stepCount = stepCountDataPoint.getBody();

        assertThat(stepCount, equalTo(expectedStepCount));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getModality(), equalTo(SENSED));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(stepCountDataPoint.getHeader().getBodySchemaId(), equalTo(StepCount2.SCHEMA_ID));
    }
}
