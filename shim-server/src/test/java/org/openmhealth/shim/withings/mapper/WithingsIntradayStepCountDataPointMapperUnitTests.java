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
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public class WithingsIntradayStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {

    protected JsonNode responseNode;
    private WithingsIntradayStepCountDataPointMapper mapper = new WithingsIntradayStepCountDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("/org/openmhealth/shim/withings/mapper/withings-intraday-activity.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(singletonList(responseNode)).size(), equalTo(4));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<StepCount2>> dataPoints = mapper.asDataPoints(responseNode);

        testIntradayStepCountDataPoint(dataPoints.get(0), 21, "2015-06-20T00:04:00Z", 60L);
        testIntradayStepCountDataPoint(dataPoints.get(1), 47, "2015-06-20T00:29:00Z", 60L);
        testIntradayStepCountDataPoint(dataPoints.get(2), 20, "2015-06-20T00:30:00Z", 60L);
        testIntradayStepCountDataPoint(dataPoints.get(3), 74, "2015-06-20T00:41:00Z", 60L);
    }

    public void testIntradayStepCountDataPoint(
            DataPoint<StepCount2> stepCountDataPoint,
            long expectedStepCountValue,
            String expectedStartDateTimeString, Long expectedDuration) {

        StepCount2 expectedStepCount =
                new StepCount2.Builder(
                        expectedStepCountValue,
                        ofStartDateTimeAndDuration(
                                OffsetDateTime.parse(expectedStartDateTimeString),
                                SECOND.newUnitValue(expectedDuration)))
                        .build();

        assertThat(stepCountDataPoint.getBody(), equalTo(expectedStepCount));
        assertThat(stepCountDataPoint.getHeader().getBodySchemaId(), equalTo(StepCount2.SCHEMA_ID));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getModality(), equalTo(SENSED));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(RESOURCE_API_SOURCE_NAME));
    }
}
