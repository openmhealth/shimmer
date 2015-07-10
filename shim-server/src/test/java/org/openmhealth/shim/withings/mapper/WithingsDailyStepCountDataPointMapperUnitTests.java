package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.*;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.*;


/**
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsDailyStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {


    private JsonNode responseNode;
    private WithingsDailyStepCountDataPointMapper mapper = new WithingsDailyStepCountDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/withings/mapper/withings-activity-measures.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsReturnsCorrectNumberOfDataPoints() {
        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(4));

    }

    @Test
    public void asDataPointsReturnsCorrectDataPoints() {
        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testDailyStepCountDataPoint(dataPoints.get(0), 2934, "2015-06-18T00:00:00-07:00", "2015-06-19T00:00:00-07:00");
        testDailyStepCountDataPoint(dataPoints.get(1), 2600, "2015-06-19T00:00:00-07:00", "2015-06-20T00:00:00-07:00");
        testDailyStepCountDataPoint(dataPoints.get(2), 5458, "2015-06-20T00:00:00-07:00", "2015-06-21T00:00:00-07:00");
        testDailyStepCountDataPoint(dataPoints.get(3), 1798, "2015-02-21T00:00:00-08:00", "2015-02-22T00:00:00-08:00");
    }

    public void testDailyStepCountDataPoint(DataPoint<StepCount> stepCountDataPoint, long expectedStepCountValue,
            String expectedDateString, String expectedEndDateString) {
        StepCount.Builder expectedStepCountBuilder = new StepCount.Builder(expectedStepCountValue);
        expectedStepCountBuilder.setEffectiveTimeFrame(TimeInterval
                .ofStartDateTimeAndEndDateTime(OffsetDateTime.parse(expectedDateString),
                        OffsetDateTime.parse(expectedEndDateString)));
        //ofStartDateTimeAndDuration(OffsetDateTime.parse(expectedDateString),new DurationUnitValue(
        //DurationUnit.DAY, 1)));
        StepCount testStepCount = stepCountDataPoint.getBody();
        StepCount expectedStepCount = expectedStepCountBuilder.build();
        assertThat(testStepCount, equalTo(expectedStepCount));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getModality(), equalTo(SENSED));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getSourceName(), equalTo(
                RESOURCE_API_SOURCE_NAME));
        assertThat(stepCountDataPoint.getHeader().getBodySchemaId(), equalTo(StepCount.SCHEMA_ID));

    }
}
