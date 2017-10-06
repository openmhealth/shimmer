/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.StepCount2;
import org.openmhealth.schema.domain.omh.TimeInterval;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.shim.moves.mapper.MovesDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Jared Sieling
 * @author Emerson Farrugia
 */
public class MovesStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MovesStepCountDataPointMapper mapper = new MovesStepCountDataPointMapper();
    private JsonNode storylineResponseNode;
    private JsonNode storylineNoSegmentsResponseNode;
    private JsonNode storylineNoStepsResponseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        storylineResponseNode = asJsonNode("/org/openmhealth/shim/moves/mapper/moves-user-storyline-daily.json");
        storylineNoSegmentsResponseNode = asJsonNode("/org/openmhealth/shim/moves/mapper/moves-user-storyline-daily-no-segments.json");
        storylineNoStepsResponseNode = asJsonNode("/org/openmhealth/shim/moves/mapper/moves-user-storyline-daily-no-steps.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<StepCount2>> dataPoints = mapper.asDataPoints(storylineResponseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(5));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfResponseHasNoSegments() throws IOException {

        assertThat(mapper.asDataPoints(storylineNoSegmentsResponseNode), empty());
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfResponseHasNoSteps() throws IOException {

        assertThat(mapper.asDataPoints(storylineNoStepsResponseNode), empty());
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<StepCount2>> dataPoints = mapper.asDataPoints(storylineResponseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndEndDateTime(
                OffsetDateTime.of(2012, 12, 12, 7, 14, 30, 0, ZoneOffset.ofHours(2)),
                OffsetDateTime.of(2012, 12, 12, 7, 27, 32, 0, ZoneOffset.ofHours(2)));

        StepCount2 stepCount = new StepCount2.Builder(1353, effectiveTimeInterval)
                .build();

        DataPoint<StepCount2> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(stepCount));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
    }
}
