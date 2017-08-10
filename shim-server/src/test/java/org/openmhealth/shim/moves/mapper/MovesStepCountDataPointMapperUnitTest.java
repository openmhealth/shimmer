package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.shim.moves.mapper.MovesDataPointMapper.RESOURCE_API_SOURCE_NAME;

/**
 * @author Jared Sieling.
 */
public class MovesStepCountDataPointMapperUnitTest extends DataPointMapperUnitTests {

    private final MovesStepCountDataPointMapper mapper = new MovesStepCountDataPointMapper();
    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        responseNode = asJsonNode("/org/openmhealth/shim/moves/mapper/moves-summary.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<StepCount1>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfEmptyResponse() throws IOException {
        JsonNode emptyNode = objectMapper.readTree("[{\"summary\": [{\n" +
                "        \"activity\": \"transport\",\n" +
                "        \"group\": \"transport\",\n" +
                "        \"duration\": 1683.0,\n" +
                "        \"distance\": 37980.0\n" +
                "      }]}]");
        assertThat(mapper.asDataPoints(emptyNode), empty());
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<StepCount1>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndDuration(
                OffsetDateTime.of(2016, 4, 3, 0, 0, 0, 0, UTC),
                new DurationUnitValue(DAY, 1));

        StepCount1 stepCount = new StepCount1.Builder(1741)
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .build();

        DataPoint<StepCount1> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(stepCount));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
    }


}
