package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.PhysicalActivity;
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
public class GoogleFitPhysicalActivityDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<PhysicalActivity> {



    private GoogleFitPhysicalActivityDataPointMapper mapper=new GoogleFitPhysicalActivityDataPointMapper();

    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/googlefit/mapper/googlefit-physical-activity.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    @Override
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(3));
    }

    @Override
    public void asDataPointsShouldReturnCorrectDataPoints() {

    }

    @Override
    public void testGoogleFitMeasureFromDataPoint(PhysicalActivity testMeasure, Map<String, Object> properties) {

    }
}
