package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.BodyHeight.*;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitBodyHeightDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<BodyHeight> {

    private final GoogleFitBodyHeightDataPointMapper mapper = new GoogleFitBodyHeightDataPointMapper();


    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-height.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointForSingleTimePoint() {

        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0),
                createFloatingPointTestProperties(1.8287990093231201, "2015-07-08T03:17:06.030Z", null,
                        "raw:com.google.height:com.google.android.apps.fitness:user_input", SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointForTimeRange() {

        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(1),
                createFloatingPointTestProperties(1.828800082206726, "2015-07-08T14:43:57.544Z",
                        "2015-07-08T14:43:58.545Z",
                        "raw:com.google.height:com.google.android.apps.fitness:user_input", SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        DataPoint<BodyHeight> selfReportedDataPoint = mapper.asDataPoints(responseNode).get(1);

        assertThat(selfReportedDataPoint.getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    @Override
    public void assertThatMeasureMatches(BodyHeight testMeasure, GoogleFitTestProperties testProperties) {

        BodyHeight.Builder expectedBodyHeightBuilder =
                new BodyHeight.Builder(new LengthUnitValue(METER, testProperties.getFpValue()));

        testProperties.getEffectiveTimeFrame().ifPresent(expectedBodyHeightBuilder::setEffectiveTimeFrame);

        assertThat(testMeasure, equalTo(expectedBodyHeightBuilder.build()));
    }
}
