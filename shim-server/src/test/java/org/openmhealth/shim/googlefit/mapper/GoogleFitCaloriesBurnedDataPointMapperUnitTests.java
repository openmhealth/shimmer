package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.CaloriesBurned;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.KcalUnit;
import org.openmhealth.schema.domain.omh.KcalUnitValue;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


/**
 * Created by Chris Schaefbauer on 7/15/15.
 */
public class GoogleFitCaloriesBurnedDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<CaloriesBurned> {

    private GoogleFitCaloriesBurnedDataPointMapper mapper = new GoogleFitCaloriesBurnedDataPointMapper();

    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/googlefit/mapper/googlefit-calories-burned.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    @Override
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(2));
    }

    @Test
    @Override
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(0),createFloatingPointTestProperties(200.0,"2015-07-07T13:30:00Z","2015-07-07T14:00:00Z","raw:com.google.calories.expended:com.google.android.apps.fitness:user_input"));
        testGoogleFitDataPoint(dataPoints.get(1),createFloatingPointTestProperties(4.221510410308838,"2015-07-08T14:43:49.730Z","2015-07-08T14:47:27.809Z","derived:com.google.calories.expended:com.google.android.gms:from_activities"));
    }

    @Override
    public void testGoogleFitMeasureFromDataPoint(CaloriesBurned testMeasure, Map<String, Object> properties) {
        CaloriesBurned.Builder expectedCaloriesBurnedBuilder = new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE,(double)properties.get("fpValue")));
        setExpectedEffectiveTimeFrame(expectedCaloriesBurnedBuilder,properties);
        CaloriesBurned expectedCaloriesBurned = expectedCaloriesBurnedBuilder.build();
        assertThat(testMeasure,equalTo(expectedCaloriesBurned));
    }
}
