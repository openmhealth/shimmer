package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount;
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
 * Created by Chris Schaefbauer on 7/14/15.
 */
public class GoogleFitStepCountDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<StepCount> {

    private GoogleFitStepCountDataPointMapper mapper = new GoogleFitStepCountDataPointMapper();

    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/googlefit/mapper/googlefit-step-count.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    @Override
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(3));
    }

    @Test
    @Override
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(0),createIntegerTestProperties(4146,"2015-02-02T22:49:39.811Z","2015-02-02T23:25:20.811Z","derived:com.google.step_count.delta:com.nike.plusgps:"));
        testGoogleFitDataPoint(dataPoints.get(1),createIntegerTestProperties(17,"2015-07-10T21:58:17.687316406Z","2015-07-10T21:59:17.687316406Z","derived:com.google.step_count.cumulative:com.google.android.gms:samsung:Galaxy Nexus:32b1bd9e:soft_step_counter"));
        testGoogleFitDataPoint(dataPoints.get(2),createIntegerTestProperties(184,"2015-07-10T22:05:21.162Z","2015-07-10T22:07:18.006058105Z","raw:com.google.step_count.delta:com.google.android.apps.fitness:user_input"));
    }

    @Override
    public void testGoogleFitMeasureFromDataPoint(StepCount testMeasure, Map<String, Object> properties) {
        StepCount.Builder expectedStepCountBuilder = new StepCount.Builder((long)properties.get("intValue"));
        setExpectedEffectiveTimeFrame(expectedStepCountBuilder,properties);
        StepCount expectedStepCount = expectedStepCountBuilder.build();
        assertThat(testMeasure,equalTo(expectedStepCount));
    }
}
