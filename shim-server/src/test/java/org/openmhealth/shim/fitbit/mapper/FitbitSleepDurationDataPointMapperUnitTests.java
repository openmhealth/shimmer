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
