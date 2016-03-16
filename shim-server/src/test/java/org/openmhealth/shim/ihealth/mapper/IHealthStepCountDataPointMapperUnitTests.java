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
import org.hamcrest.Matchers;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount;
import org.openmhealth.schema.domain.omh.TimeInterval;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.StepCount.SCHEMA_ID;


/**
 * @author Chris Schaefbauer
 */
public class IHealthStepCountDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private IHealthStepCountDataPointMapper mapper = new IHealthStepCountDataPointMapper();
    private List<DataPoint<StepCount>> dataPoints;


    @BeforeClass
    public void initializeResponseNode() {

        responseNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-activity.json");
    }

    @BeforeMethod
    public void initializeDataPoints() {

        dataPoints = mapper.asDataPoints(responseNode);
    }

    @Test
    public void asDataPointsShouldNotMapDataPointsWithZeroSteps() {

        JsonNode nodeWithNoSteps = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-activity-no-steps.json");

        assertThat(mapper.asDataPoints(nodeWithNoSteps), is(empty()));
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenSensed() {

        StepCount expectedStepCount = new StepCount.Builder(21)
                .setEffectiveTimeFrame(
                        TimeInterval.ofStartDateTimeAndDuration(OffsetDateTime.parse("2015-11-16T00:00:00+05:00"),
                                new DurationUnitValue(DAY, 1)))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedStepCount));

        testDataPointHeader(dataPoints.get(0).getHeader(), SCHEMA_ID, SENSED,
                "ac67c4ccf64af669d92569af85d19f59", OffsetDateTime.parse("2015-11-17T19:23:21Z"));
    }

    @Test
    public void asDataPointsShouldReturnDataPointWithUserNoteWhenNoteIsPresent() {

        StepCount expectedStepCount = new StepCount.Builder(4398)
                .setEffectiveTimeFrame(
                        TimeInterval.ofStartDateTimeAndDuration(OffsetDateTime.parse("2015-11-18T00:00:00Z"),
                                new DurationUnitValue(DAY, 1))).setUserNotes("Great steps")
                .build();

        assertThat(dataPoints.get(1).getBody(), Matchers.equalTo(expectedStepCount));

        assertThat(dataPoints.get(0).getBody().getUserNotes(), nullValue());
        assertThat(dataPoints.get(1).getBody().getUserNotes(), equalTo("Great steps"));
    }

    @Test
    public void asDataPointsShouldReturnSensedDataPointWhenManuallyEntered() {

        assertThat(mapper.asDataPoints(responseNode).get(1).getHeader().getAcquisitionProvenance().getModality(),
                equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListWhenEmptyIHealthResponse() {

        JsonNode emptyNode = asJsonNode("/org/openmhealth/shim/ihealth/mapper/ihealth-activity-empty.json");

        assertThat(mapper.asDataPoints(emptyNode), is(empty()));
    }
}
