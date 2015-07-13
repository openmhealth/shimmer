package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;
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
 * Created by Chris Schaefbauer on 7/12/15.
 */
public class GoogleFitBodyWeightDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<BodyWeight> {


    private GoogleFitBodyWeightDataPointMapper mapper = new GoogleFitBodyWeightDataPointMapper();

    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/googlefit/mapper/googlefit-body-weight.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    @Override
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(
                singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(3));
    }

    @Test
    @Override
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(0), createFloatingPointTestProperties(72.0999984741211,"2015-02-13T00:00:00Z",null,
                "raw:com.google.weight:com.fatsecret.android:"));
        testGoogleFitDataPoint(dataPoints.get(1), createFloatingPointTestProperties(72,"2015-02-17T16:57:13.313Z",null,
                "raw:com.google.weight:com.wsl.noom:"));
        testGoogleFitDataPoint(dataPoints.get(2), createFloatingPointTestProperties(75.75070190429688,"2015-07-08T03:17:00Z","2015-07-08T03:17:10.020Z",
                "raw:com.google.weight:com.google.android.apps.fitness:user_input"));

    }

    @Override
    public void testGoogleFitMeasureFromDataPoint(BodyWeight testMeasure, Map<String, Object> properties) {
        BodyWeight.Builder expectedBodyWeightBuilder =
                new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,(Double)properties.get("fpValue")));
//        if(properties.containsKey("endDateTimeString")){
//            expectedBodyWeightBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
//                    OffsetDateTime.parse((String) properties.get("startDateTimeString")),
//                    OffsetDateTime.parse((String) properties.get("endDateTimeString"))));
//        }
//        else{
//            expectedBodyWeightBuilder.setEffectiveTimeFrame(OffsetDateTime.parse((String)properties.get("startDateTimeString")));
//        }
        setExpectedEffectiveTimeFrame(expectedBodyWeightBuilder,properties);
        BodyWeight expectedBodyWeight = expectedBodyWeightBuilder.build();
        assertThat(testMeasure,equalTo(expectedBodyWeight));
    }

}
