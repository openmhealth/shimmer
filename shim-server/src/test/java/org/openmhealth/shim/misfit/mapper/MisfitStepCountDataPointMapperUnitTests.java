package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matchers;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.shim.misfit.mapper.MisfitDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Emerson Farrugia
 */
public class MisfitStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MisfitStepCountDataPointMapper mapper = new MisfitStepCountDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/misfit/mapper/misfit-detailed-summaries.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        // FIXME fix the time zone offset once Misfit add it to the API
        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndDuration(
                OffsetDateTime.of(2015, 4, 13, 0, 0, 0, 0, UTC),
                new DurationUnitValue(DAY, 1));

        StepCount stepCount = new StepCount.Builder(26370)
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .build();

        DataPoint<StepCount> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(stepCount));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfEmptyResponse() throws IOException {

        JsonNode emptyNode = objectMapper.readTree("{\n" +
                "    \"summary\": []\n" +
                "}");
        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(emptyNode));

        assertThat(dataPoints.size(), Matchers.equalTo(0));
    }
}