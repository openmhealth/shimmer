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
 * Created by Chris Schaefbauer on 7/5/15.
 */
public class WithingsDailyCaloriesBurnedDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    private WithingsDailyCaloriesBurnedDataPointMapper mapper = new WithingsDailyCaloriesBurnedDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/withings/mapper/withings-activity-measures.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(4));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testDailyCaloriesBurnedDataPoint(dataPoints.get(0), 139, "2015-06-18T00:00:00-07:00");
        testDailyCaloriesBurnedDataPoint(dataPoints.get(1), 130, "2015-06-19T00:00:00-07:00");
        testDailyCaloriesBurnedDataPoint(dataPoints.get(2), 241, "2015-06-20T00:00:00-07:00");
        testDailyCaloriesBurnedDataPoint(dataPoints.get(3), 99, "2015-02-21T00:00:00-08:00");

    }

    public void testDailyCaloriesBurnedDataPoint(DataPoint<CaloriesBurned> caloriesBurnedDataPoint, long expectedCaloriesBurnedValue, String expectedDateString){
        CaloriesBurned.Builder expectedCaloriesBurnedBuilder = new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE,expectedCaloriesBurnedValue));
        expectedCaloriesBurnedBuilder.setEffectiveTimeFrame(
                TimeInterval.ofStartDateTimeAndDuration(OffsetDateTime.parse(expectedDateString), new DurationUnitValue(
                        DurationUnit.DAY, 1)));
        CaloriesBurned testCaloriesBurned = caloriesBurnedDataPoint.getBody();
        CaloriesBurned expectedCaloriesBurned = expectedCaloriesBurnedBuilder.build();
        assertThat(testCaloriesBurned,equalTo(expectedCaloriesBurned));
        assertThat(caloriesBurnedDataPoint.getHeader().getAcquisitionProvenance().getModality(),equalTo(SENSED));
        assertThat(caloriesBurnedDataPoint.getHeader().getAcquisitionProvenance().getSourceName(),equalTo(
                RESOURCE_API_SOURCE_NAME));
        assertThat(caloriesBurnedDataPoint.getHeader().getBodySchemaId(),equalTo(CaloriesBurned.SCHEMA_ID));

    }



}
