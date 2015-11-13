package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.*;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitBodyWeightDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<BodyWeight> {

    private GoogleFitBodyWeightDataPointMapper mapper = new GoogleFitBodyWeightDataPointMapper();


    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/googlefit/mapper/googlefit-body-weight.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForSingleTimePoint() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(0),
                createFloatingPointTestProperties(72.0999984741211, "2015-02-13T00:00:00Z", null,
                        "raw:com.google.weight:com.fatsecret.android:"));
        testGoogleFitDataPoint(dataPoints.get(1),
                createFloatingPointTestProperties(72, "2015-02-17T16:57:13.313Z", null,
                        "raw:com.google.weight:com.wsl.noom:"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWithTimeRange() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        testGoogleFitDataPoint(dataPoints.get(2),
                createFloatingPointTestProperties(75.75070190429688, "2015-07-08T03:17:00Z", "2015-07-08T03:17:10.020Z",
                        "raw:com.google.weight:com.google.android.apps.fitness:user_input"));
    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        // TODO split
        assertThat(dataPoints.get(2).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getModality(), nullValue());

    }

    /* Helper methods */
    // TODO clean up
    @Override
    public void testGoogleFitMeasureFromDataPoint(BodyWeight testMeasure, Map<String, Object> properties) {
        BodyWeight.Builder expectedBodyWeightBuilder =
                new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, (Double) properties.get("fpValue")));
        setExpectedEffectiveTimeFrame(expectedBodyWeightBuilder, properties);
        BodyWeight expectedBodyWeight = expectedBodyWeightBuilder.build();
        assertThat(testMeasure, equalTo(expectedBodyWeight));
    }
}
