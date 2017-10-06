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

package org.openmhealth.shim.fitbit.mapper;

import org.openmhealth.schema.domain.omh.*;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.PercentUnit.PERCENT;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Emerson Farrugia
 */
public class FitbitSleepEpisodeDataPointMapperUnitTests extends FitbitSleepMeasureDataPointMapperUnitTests<SleepEpisode> {

    private final FitbitSleepEpisodeDataPointMapper mapper = new FitbitSleepEpisodeDataPointMapper();

    @Override
    public FitbitSleepEpisodeDataPointMapper getMapper() {
        return mapper;
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        SleepEpisode expectedSleepEpisode = new SleepEpisode.Builder(
                ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2016-12-13T01:16:00.000Z"),
                        OffsetDateTime.parse("2016-12-13T03:14:00.000Z")
                ))
                .setLatencyToSleepOnset(new DurationUnitValue(MINUTE, 0))
                .setLatencyToArising(new DurationUnitValue(MINUTE, 0))
                .setTotalSleepTime(new DurationUnitValue(MINUTE, 112))
                .setMainSleep(true)
                .setSleepMaintenanceEfficiencyPercentage(new TypedUnitValue<>(PERCENT, 95))
                .build();

        List<DataPoint<SleepEpisode>> dataPoints = mapper.asDataPoints(sleepDateResponseNode);

        DataPoint<SleepEpisode> dataPoint = dataPoints.get(1);

        assertThat(dataPoint.getBody(), equalTo(expectedSleepEpisode));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(SleepEpisode.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }
}
