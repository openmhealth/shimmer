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

package org.openmhealth.shim.misfit.mapper;

import org.openmhealth.schema.domain.omh.*;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.shim.misfit.mapper.MisfitDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Emerson Farrugia
 */
public class MisfitSleepEpisodeDataPointMapperUnitTests
        extends MisfitSleepMeasureDataPointMapperUnitTests<SleepEpisode> {

    private final MisfitSleepEpisodeDataPointMapper mapper = new MisfitSleepEpisodeDataPointMapper();

    @Override
    protected MisfitSleepMeasureDataPointMapper<SleepEpisode> getMapper() {
        return mapper;
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        // the end time is the addition of the start time and the total duration when the last segment isn't awake
        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndEndDateTime(
                OffsetDateTime.of(2015, 2, 23, 21, 40, 59, 0, ZoneOffset.ofHours(-5)),
                OffsetDateTime.of(2015, 2, 24, 0, 53, 59, 0, ZoneOffset.ofHours(-5)));

        SleepEpisode expectedSleepEpisode = new SleepEpisode.Builder(effectiveTimeInterval)
                // the total sleep time is total duration minus the sum of the awake segment durations
                .setTotalSleepTime(new DurationUnitValue(SECOND, 10140))
                .build();

        List<DataPoint<SleepEpisode>> dataPoints = mapper.asDataPoints(sleepsResponseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        DataPoint<SleepEpisode> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(expectedSleepEpisode));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getModality(), equalTo(SENSED));
    }
}
