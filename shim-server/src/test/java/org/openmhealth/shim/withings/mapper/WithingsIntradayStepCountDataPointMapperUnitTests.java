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
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * Created by Chris Schaefbauer on 7/2/15.
 */
public class WithingsIntradayStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {

    protected JsonNode responseNode;
    private WithingsIntradayStepCountDataPointMapper mapper = new WithingsIntradayStepCountDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/withings/mapper/withings-intraday-activity.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(4));

    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testIntradayStepCountDataPoint(dataPoints.get(0),21,"2015-06-20T00:04:00Z",60L);
        testIntradayStepCountDataPoint(dataPoints.get(1),47,"2015-06-20T00:29:00Z",60L);
        testIntradayStepCountDataPoint(dataPoints.get(2),20,"2015-06-20T00:30:00Z",60L);
        testIntradayStepCountDataPoint(dataPoints.get(3),74,"2015-06-20T00:41:00Z",60L);
    }

    public void testIntradayStepCountDataPoint(DataPoint<StepCount> stepCountDataPoint, long expectedStepCountValue,
            String expectedDateString, Long expectedDuration) {
        StepCount.Builder expectedStepCountBuilder = new StepCount.Builder(expectedStepCountValue);
        expectedStepCountBuilder.setEffectiveTimeFrame(
                TimeInterval.ofStartDateTimeAndDuration(OffsetDateTime.parse(expectedDateString), new DurationUnitValue(
                        DurationUnit.SECOND, expectedDuration)));
        StepCount testStepCount = stepCountDataPoint.getBody();
        StepCount expectedStepCount = expectedStepCountBuilder.build();
        assertThat(testStepCount, equalTo(expectedStepCount));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getModality(), equalTo(SENSED));
        assertThat(stepCountDataPoint.getHeader().getAcquisitionProvenance().getSourceName(), equalTo(
                RESOURCE_API_SOURCE_NAME));
        assertThat(stepCountDataPoint.getHeader().getBodySchemaId(), equalTo(StepCount.SCHEMA_ID));

    }


}
