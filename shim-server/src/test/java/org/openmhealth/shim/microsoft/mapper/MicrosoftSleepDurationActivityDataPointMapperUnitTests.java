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
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.shim.microsoft.mapper.MicrosoftDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Wallace Wadge
 */
public class MicrosoftSleepDurationActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MicrosoftSleepDurationDataPointMapper mapper = new MicrosoftSleepDurationDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/microsoft/mapper/microsoft-sleepduration.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndEndDateTime(
                OffsetDateTime.of(2015, 5, 11, 8, 0, 0, 0, UTC),
                OffsetDateTime.of(2015, 5, 11, 14, 00, 0, 0, UTC));

        SleepDuration measure = new SleepDuration.Builder(new DurationUnitValue(SECOND, 1244520))
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .build();

        DataPoint<SleepDuration> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(measure));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
    }

}