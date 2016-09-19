package org.openmhealth.shim.microsoft.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.shim.microsoft.mapper.MicrosoftDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Emerson Farrugia
 */
public class MicrosoftHeartRateDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MicrosoftHeartRateDataPointMapper mapper = new MicrosoftHeartRateDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/microsoft/mapper/microsoft-heartrate.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {


        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));
        HeartRate.Builder expectedHeartRateBuilder =
                new HeartRate.Builder(123);

        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndDuration(
                OffsetDateTime.of(2016, 9, 14, 0, 0, 0, 0, UTC),
                new DurationUnitValue(DAY, 1));

        expectedHeartRateBuilder.setEffectiveTimeFrame(effectiveTimeInterval);

        DataPoint<HeartRate> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(expectedHeartRateBuilder.build()));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfEmptyResponse() throws IOException {

        JsonNode emptyNode = objectMapper.readTree("{\n" +
                "    \"summaries\": []\n" +
                "}");

        assertThat(mapper.asDataPoints(emptyNode), empty());
    }
}