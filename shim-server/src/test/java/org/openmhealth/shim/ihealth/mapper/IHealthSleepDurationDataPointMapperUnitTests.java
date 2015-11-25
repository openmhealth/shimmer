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
import org.openmhealth.schema.domain.omh.SleepDuration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Chris Schaefbauer
 */
public class IHealthSleepDurationDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private IHealthSleepDurationDataPointMapper mapper = new IHealthSleepDurationDataPointMapper();
    private List<DataPoint<SleepDuration>> dataPoints;

    @BeforeClass
    public void initializeResponse() {

        responseNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-sleep.json");
    }

    @BeforeMethod
    public void initializeDataPoints() {

        dataPoints = mapper.asDataPoints(singletonList(responseNode));
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenSensed() {

        SleepDuration.Builder expectedSleepDurationBuilder = new SleepDuration.Builder(new DurationUnitValue(
                MINUTE, 345));

        expectedSleepDurationBuilder.setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(
                OffsetDateTime.parse("2015-11-15T01:51:00-07:00"),
                OffsetDateTime.parse("2015-11-15T09:16:00-07:00")));

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepDurationBuilder.build()));

        testDataPointHeader(dataPoints.get(0).getHeader(), SleepDuration.SCHEMA_ID, SENSED,
                "7eb7292b90d710ae7b7f61b75f9425cf", OffsetDateTime.parse("2015-11-15T16:19:10Z"));
    }

    @Test
    public void asDataPointsShouldMapAwakenAsAdditionalProperty() {

        assertThat(((BigDecimal) dataPoints.get(0).getBody().getAdditionalProperties().get("wakeup_count")).intValue(),
                equalTo(13));

        assertThat(((BigDecimal) dataPoints.get(2).getBody().getAdditionalProperties().get("wakeup_count")).intValue(),
                equalTo(0));
    }

    @Test
    public void asDataPointsShouldReturnDataPointWithUserNoteWhenNoteIsPresent() {

        SleepDuration.Builder expectedSleepDurationBuilder =
                new SleepDuration.Builder(new DurationUnitValue(MINUTE, 195));

        expectedSleepDurationBuilder.setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(
                OffsetDateTime.parse("2015-11-15T13:51:00+01:00"),
                OffsetDateTime.parse("2015-11-15T17:16:00+01:00")));

        expectedSleepDurationBuilder.setUserNotes("Best sleep ever");

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedSleepDurationBuilder.build()));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenManuallyEntered() {

        assertThat(dataPoints.get(2).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }
}
