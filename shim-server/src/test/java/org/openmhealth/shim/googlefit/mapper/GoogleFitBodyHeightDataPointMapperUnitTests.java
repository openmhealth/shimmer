package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.schema.domain.omh.TimeInterval;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitBodyHeightDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<BodyHeight> {

    private GoogleFitBodyHeightDataPointMapper mapper = new GoogleFitBodyHeightDataPointMapper();


    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-height.json");
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

        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    /* Helper methods */
    // TODO clean up
    @Override
    public void testGoogleFitMeasureFromDataPoint(BodyHeight testMeasure, Map<String, Object> properties) {

        BodyHeight.Builder bodyHeightBuilder =
                new BodyHeight.Builder(new LengthUnitValue(METER, (double) properties.get("fpValue")));
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
