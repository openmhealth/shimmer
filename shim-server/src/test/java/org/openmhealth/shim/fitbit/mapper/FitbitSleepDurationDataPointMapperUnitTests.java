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

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration2;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class FitbitSleepDurationDataPointMapperUnitTests extends FitbitSleepMeasureDataPointMapperUnitTests<SleepDuration2> {

    private final FitbitSleepDurationDataPointMapper mapper = new FitbitSleepDurationDataPointMapper();

    @Override
    public FitbitSleepDurationDataPointMapper getMapper() {
        return mapper;
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        SleepDuration2 expectedSleepDuration = new SleepDuration2.Builder(
                new DurationUnitValue(MINUTE, 112),
                ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2016-12-13T01:16:00.000Z"),
                        OffsetDateTime.parse("2016-12-13T03:14:00.000Z")
                ))
                .build();

        List<DataPoint<SleepDuration2>> dataPoints = mapper.asDataPoints(sleepDateResponseNode);

        DataPoint<SleepDuration2> dataPoint = dataPoints.get(1);

        assertThat(dataPoint.getBody(), equalTo(expectedSleepDuration));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(SleepDuration2.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }
}
