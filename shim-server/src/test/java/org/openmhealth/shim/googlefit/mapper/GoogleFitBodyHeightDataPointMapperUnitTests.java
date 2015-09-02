package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.*;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitBodyHeightDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<BodyHeight> {


    private GoogleFitBodyHeightDataPointMapper mapper = new GoogleFitBodyHeightDataPointMapper();

    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/googlefit/mapper/googlefit-body-height.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointForSingleTimePoint() {

        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(0),
                createFloatingPointTestProperties(1.8287990093231201, "2015-07-08T03:17:06.030Z", null,
                        "raw:com.google.height:com.google.android.apps.fitness:user_input"));

    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointForTimeRange() {

        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(1),
                createFloatingPointTestProperties(1.828800082206726, "2015-07-08T14:43:57.544Z",
                        "2015-07-08T14:43:58.545Z",
                        "raw:com.google.height:com.google.android.apps.fitness:user_input"));
    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(),
                equalTo(DataPointModality.SELF_REPORTED));

    }

    /* Helper methods */

    @Override
    public void testGoogleFitMeasureFromDataPoint(BodyHeight testMeasure, Map<String, Object> properties) {

        BodyHeight.Builder bodyHeightBuilder =
                new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER, (double) properties.get("fpValue")));
        if (properties.containsKey("endDateTimeString")) {
            bodyHeightBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                    OffsetDateTime.parse((String) properties.get("startDateTimeString")),
                    OffsetDateTime.parse((String) properties.get("endDateTimeString"))));
        }
        else {
            bodyHeightBuilder
                    .setEffectiveTimeFrame(OffsetDateTime.parse((String) properties.get("startDateTimeString")));
        }
        BodyHeight expectedBodyHeight = bodyHeightBuilder.build();
        assertThat(testMeasure, equalTo(expectedBodyHeight));
    }
}
