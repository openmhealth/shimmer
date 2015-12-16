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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnit;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.fitbit.mapper.FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public class FitbitIntradayStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {


    private JsonNode responseNode;
    private FitbitIntradayStepCountDataPointMapper mapper = new FitbitIntradayStepCountDataPointMapper();


    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("/org/openmhealth/shim/fitbit/mapper/fitbit-activities-steps-1d-1m-intraday.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        StepCount.Builder stepCountBuilder = new StepCount.Builder(7)
                .setEffectiveTimeFrame(ofStartDateTimeAndDuration(OffsetDateTime.parse("2015-08-21T00:00:00Z"),
                        new DurationUnitValue(DurationUnit.MINUTE, 1)));

        assertThat(dataPoints.get(0).getBody(), equalTo(stepCountBuilder.build()));
        assertThat(dataPoints.get(0).getHeader().getBodySchemaId(), equalTo(StepCount.SCHEMA_ID));
        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(RESOURCE_API_SOURCE_NAME));

        stepCountBuilder = new StepCount.Builder(52)
                .setEffectiveTimeFrame(ofStartDateTimeAndDuration(OffsetDateTime.parse("2015-08-21T14:28:00Z"),
                        new DurationUnitValue(DurationUnit.MINUTE, 1)));

        assertThat(dataPoints.get(1).getBody(), equalTo(stepCountBuilder.build()));
        assertThat(dataPoints.get(1).getHeader().getBodySchemaId(), equalTo(StepCount.SCHEMA_ID));
        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(RESOURCE_API_SOURCE_NAME));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenDataSetArrayIsEmpty() throws IOException {

        JsonNode emptyDataSetNode = objectMapper.readTree(
            "{\n" +
                "\"activities-steps\": [ \n" +
                    "{\n" +
                        "\"dateTime\": \"2015-05-24\"\n," +
                        "\"value\": 0\n" +
                    "}\n" +
                "],\n" +
                "\"activities-steps-intraday\": {\n" +
                    "\"dataset\": [],\n" +
                    "\"datasetInterval\": 1,\n" +
                    "\"datasetType\": \"minute\"\n" +
                "}\n" +
            "}");

        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(emptyDataSetNode));
        assertThat(dataPoints.isEmpty(), equalTo(true));
    }
}
