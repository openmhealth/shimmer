package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration;
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
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;


/**
 * @author Chris Schaefbauer
 */
public class FitbitSleepDurationDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final FitbitSleepDurationDataPointMapper mapper = new FitbitSleepDurationDataPointMapper();
    private JsonNode singleSleepResponseNode;
    private JsonNode multipleSleepResponseNode;


    @BeforeTest
    public void initializeResponseNode() throws IOException {

        singleSleepResponseNode = asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date.json");
        multipleSleepResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-multiple-in-sleep-list.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(singleSleepResponseNode).size(), equalTo(1));
        assertThat(mapper.asDataPoints(multipleSleepResponseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        SleepDuration expectedSleepDuration = new SleepDuration.Builder(new DurationUnitValue(MINUTE, 831))
                .setEffectiveTimeFrame(ofStartDateTimeAndDuration(
                        OffsetDateTime.parse("2014-07-19T11:58:00Z"), new DurationUnitValue(MINUTE, 961)))
                .build();

        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(singleSleepResponseNode);

        DataPoint<SleepDuration> dataPoint = dataPoints.get(0);

        assertThat(dataPoint.getBody(), equalTo(expectedSleepDuration));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(SleepDuration.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListWhenResponseIsEmpty() throws IOException {

        JsonNode responseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-empty-sleep-list.json");

        assertThat(mapper.asDataPoints(responseNode), is(empty()));
    }
}
