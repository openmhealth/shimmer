/*
 * Copyright 2017 Open mHealth
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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static java.time.OffsetDateTime.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Emerson Farrugia
 */
public class WithingsSleepEpisodeDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    private WithingsSleepEpisodeDataPointMapper mapper = new WithingsSleepEpisodeDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/withings/mapper/withings-sleep-summary.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<SleepEpisode>> dataPoints = mapper.asDataPoints(responseNode);

        SleepEpisode expectedSleepEpisode = new SleepEpisode.Builder(
                ofStartDateTimeAndEndDateTime(parse("2017-09-02T00:19:00+01:00"), parse("2017-09-02T03:25:00+01:00"))
        )
                .setLatencyToSleepOnset(new DurationUnitValue(SECOND, 240))
                .setLatencyToArising(new DurationUnitValue(SECOND, 0))
                .setTotalSleepTime(new DurationUnitValue(SECOND, 11160))
                .setNumberOfAwakenings(4)
                .build();

        assertThat(dataPoints.get(0).getHeader().getBodySchemaId(), equalTo(SleepEpisode.SCHEMA_ID));
        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepEpisode));

        DataPointAcquisitionProvenance acquisitionProvenance = dataPoints.get(0).getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance.getSourceName(), equalTo(WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getModality(), equalTo(SENSED));
        assertThat(acquisitionProvenance.getAdditionalProperties().get("external_id"), equalTo("123"));
        assertThat(acquisitionProvenance.getAdditionalProperties().get("device_name"), equalTo("Activity tracker"));
    }
}
