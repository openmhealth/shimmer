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
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


// TODO clean up
/**
 * Created by Chris Schaefbauer on 7/5/15.
 */
public class WithingsIntradayCaloriesBurnedDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    private WithingsIntradayCaloriesBurnedDataPointMapper mapper = new WithingsIntradayCaloriesBurnedDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/withings/mapper/withings-intraday-activity.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(4));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testIntradayCaloriesBurnedDataPoint(dataPoints.get(0), 1, "2015-06-20T00:04:00Z", 60L);
        testIntradayCaloriesBurnedDataPoint(dataPoints.get(1), 2, "2015-06-20T00:29:00Z", 60L);
        testIntradayCaloriesBurnedDataPoint(dataPoints.get(2), 1, "2015-06-20T00:30:00Z", 60L);
        testIntradayCaloriesBurnedDataPoint(dataPoints.get(3), 7, "2015-06-20T00:41:00Z", 60L);


    }

    public void testIntradayCaloriesBurnedDataPoint(DataPoint<CaloriesBurned> caloriesBurnedDataPoint,
            long expectedCaloriesBurnedValue,
            String expectedDateString, Long expectedDuration) {
        CaloriesBurned.Builder expectedCaloriesBurnedBuilder =
                new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE, expectedCaloriesBurnedValue));

        expectedCaloriesBurnedBuilder.setEffectiveTimeFrame(
                TimeInterval.ofStartDateTimeAndDuration(OffsetDateTime.parse(expectedDateString), new DurationUnitValue(
                        DurationUnit.SECOND, expectedDuration)));
        CaloriesBurned testCaloriesBurned = caloriesBurnedDataPoint.getBody();
        CaloriesBurned expectedCaloriesBurned = expectedCaloriesBurnedBuilder.build();
        assertThat(testCaloriesBurned, equalTo(expectedCaloriesBurned));
        assertThat(caloriesBurnedDataPoint.getHeader().getAcquisitionProvenance().getModality(), equalTo(SENSED));
        assertThat(caloriesBurnedDataPoint.getHeader().getAcquisitionProvenance().getSourceName(), equalTo(
                RESOURCE_API_SOURCE_NAME));
        assertThat(caloriesBurnedDataPoint.getHeader().getBodySchemaId(), equalTo(CaloriesBurned.SCHEMA_ID));

    }

}
