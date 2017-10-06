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
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration2;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.SleepDuration2.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Chris Schaefbauer
 */
public class IHealthSleepDurationDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private IHealthSleepDurationDataPointMapper mapper = new IHealthSleepDurationDataPointMapper();
    private List<DataPoint<SleepDuration2>> dataPoints;

    @BeforeClass
    public void initializeResponse() {

        responseNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-sleep.json");
    }

    @BeforeMethod
    public void initializeDataPoints() {

        dataPoints = mapper.asDataPoints(responseNode);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenSensed() {

        SleepDuration2 expectedSleepDuration = new SleepDuration2.Builder(
                new DurationUnitValue(MINUTE, 345),
                ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2015-11-15T01:51:00-07:00"),
                        OffsetDateTime.parse("2015-11-15T09:16:00-07:00")))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepDuration));

        testDataPointHeader(dataPoints.get(0).getHeader(), SCHEMA_ID, SENSED,
                "7eb7292b90d710ae7b7f61b75f9425cf", OffsetDateTime.parse("2015-11-15T16:19:10Z"));
    }

    @Test
    public void asDataPointsShouldReturnDataPointWithUserNoteWhenNoteIsPresent() {

        SleepDuration2 expectedSleepDuration = new SleepDuration2.Builder(
                new DurationUnitValue(MINUTE, 195),
                ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2015-11-15T13:51:00+01:00"),
                        OffsetDateTime.parse("2015-11-15T17:16:00+01:00")))
                .setUserNotes("Best sleep ever")
                .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedSleepDuration));
        assertThat(dataPoints.get(1).getBody().getUserNotes(), equalTo("Best sleep ever"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenManuallyEntered() {

        assertThat(dataPoints.get(2).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListWhenEmptyIHealthResponse() {

        JsonNode emptyNode = asJsonNode("/org/openmhealth/shim/ihealth/mapper/ihealth-sleep-empty.json");

        assertThat(mapper.asDataPoints(emptyNode), is(empty()));
    }
}
