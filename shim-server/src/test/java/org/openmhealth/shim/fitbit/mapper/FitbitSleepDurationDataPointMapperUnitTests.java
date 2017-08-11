package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration2;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.DurationUnit.MINUTE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Chris Schaefbauer
 */
public class FitbitSleepDurationDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final FitbitSleepDurationDataPointMapper mapper = new FitbitSleepDurationDataPointMapper();
    private JsonNode sleepDateResponseNode;
    private JsonNode sleepDateEmptySleepListResponseNode;
    private JsonNode sleepDateRangeResponseNode;
    private JsonNode sleepDateRangeEmptySleepListResponseNode;


    @BeforeTest
    public void initializeResponseNode() throws IOException {

        sleepDateResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date.json");
        sleepDateEmptySleepListResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-empty-sleep-list.json");
        sleepDateRangeResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-range.json");
        sleepDateRangeEmptySleepListResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-range-empty-sleep-list.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(sleepDateResponseNode).size(), equalTo(2));
        assertThat(mapper.asDataPoints(sleepDateEmptySleepListResponseNode), is(empty()));
        assertThat(mapper.asDataPoints(sleepDateRangeResponseNode).size(), equalTo(2));
        assertThat(mapper.asDataPoints(sleepDateRangeEmptySleepListResponseNode), is(empty()));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        SleepDuration2 expectedSleepDuration = new SleepDuration2.Builder(
                new DurationUnitValue(MINUTE, 63),
                ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2016-12-13T09:59:00.000Z"),
                        OffsetDateTime.parse("2016-12-13T11:02:00.000Z")
                ))
                .build();

        List<DataPoint<SleepDuration2>> dataPoints = mapper.asDataPoints(sleepDateResponseNode);

        DataPoint<SleepDuration2> dataPoint = dataPoints.get(0);

        assertThat(dataPoint.getBody(), equalTo(expectedSleepDuration));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(SleepDuration2.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }
}
