package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;
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
 * @author Chris Schaefbauer
 */
public class GoogleFitHeartRateDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<HeartRate> {

    private GoogleFitHeartRateDataPointMapper mapper = new GoogleFitHeartRateDataPointMapper();


    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/googlefit/mapper/googlefit-heart-rate.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForSingleTimePoint() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(0),
                createFloatingPointTestProperties(54, "2015-01-30T15:37:48.186Z", null,
                        "raw:com.google.heart_rate.bpm:com.azumio.instantheartrate.full:"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForTimeRange() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(1),
                createFloatingPointTestProperties(58.0, "2015-07-10T14:34:32.914Z", "2015-07-10T14:34:33.915Z",
                        "raw:com.google.heart_rate.bpm:si.modula.android.instantheartrate:"));
    }

    /* Helper methods */
    // TODO clean up
    @Override
    public void testGoogleFitMeasureFromDataPoint(HeartRate testMeasure, Map<String, Object> properties) {

        HeartRate.Builder expectedHeartRateBuilder = new HeartRate.Builder((double) properties.get("fpValue"));
        setExpectedEffectiveTimeFrame(expectedHeartRateBuilder, properties);
        HeartRate expectedHeartRate = expectedHeartRateBuilder.build();

        assertThat(testMeasure, equalTo(expectedHeartRate));
    }
}
