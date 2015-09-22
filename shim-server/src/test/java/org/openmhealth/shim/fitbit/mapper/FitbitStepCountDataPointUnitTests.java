package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.CoreMatchers;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


/**
 * @author Chris Schaefbauer
 */
public class FitbitStepCountDataPointUnitTests extends DataPointMapperUnitTests {

    JsonNode responseNodeStepTimeSeries;
    protected final FitbitStepCountDataPointMapper mapper = new FitbitStepCountDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-time-series-steps.json");
        responseNodeStepTimeSeries = objectMapper.readTree(resource.getInputStream());

    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<StepCount>> stepDataPoints = mapper.asDataPoints(singletonList(responseNodeStepTimeSeries));
        assertThat(stepDataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<StepCount>> stepDataPoints = mapper.asDataPoints(singletonList(responseNodeStepTimeSeries));

        testFitbitStepCountDataPoint(stepDataPoints.get(0), 2170, "2015-05-26");
        testFitbitStepCountDataPoint(stepDataPoints.get(1), 3248, "2015-05-27");
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointWhenStepCountEqualsZero() throws IOException {

        JsonNode zeroStepsNode = objectMapper.readTree(
                "{\n" +
                        "\"activities-steps\": [ \n" +
                        "{\n" +
                        "\"dateTime\": \"2015-05-24\"\n," +
                        "\"value\": \"0\"\n" +
                        "}\n" +
                        "]\n" +
                        "}");

        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(zeroStepsNode));
        assertThat(dataPoints.size(),equalTo(0));

    }

    public void testFitbitStepCountDataPoint(DataPoint<StepCount> dataPoint, long stepCount, String dateString) {

        StepCount.Builder dataPointBuilderForExpected = new StepCount.Builder(stepCount);

        OffsetDateTime startDateTime =
                OffsetDateTime.parse(dateString + "T" + "00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        startDateTime = startDateTime.withNano(000000000);

        dataPointBuilderForExpected.setEffectiveTimeFrame(
                TimeInterval.ofStartDateTimeAndDuration(startDateTime, new DurationUnitValue(DurationUnit.DAY, 1)));
        StepCount expectedStepCount = dataPointBuilderForExpected.build();

        assertThat(dataPoint.getBody(), CoreMatchers.equalTo(expectedStepCount));
        assertThat(dataPoint.getHeader().getBodySchemaId(), CoreMatchers.equalTo(StepCount.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                CoreMatchers.nullValue());
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                CoreMatchers.equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));

    }

}
