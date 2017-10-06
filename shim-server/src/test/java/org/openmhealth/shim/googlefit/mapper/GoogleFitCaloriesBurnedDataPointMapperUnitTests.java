package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.CaloriesBurned2;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.openmhealth.schema.domain.omh.CaloriesBurned2.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitCaloriesBurnedDataPointMapperUnitTests
        extends GoogleFitDataPointMapperUnitTests<CaloriesBurned2> {

    private final GoogleFitCaloriesBurnedDataPointMapper mapper = new GoogleFitCaloriesBurnedDataPointMapper();

    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-calories-expended.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<CaloriesBurned2>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0),
                createFloatingPointTestProperties(200.0, "2015-07-07T13:30:00Z", "2015-07-07T14:00:00Z",
                        "raw:com.google.calories.expended:com.google.android.apps.fitness:user_input", SCHEMA_ID));

        assertThatDataPointMatches(dataPoints.get(1),
                createFloatingPointTestProperties(4.221510410308838, "2015-07-08T14:43:49.730Z",
                        "2015-07-08T14:47:27.809Z",
                        "derived:com.google.calories.expended:com.google.android.gms:from_activities", SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        assertThat(mapper.asDataPoints(responseNode).get(0).getHeader().getAcquisitionProvenance().getModality(),
                equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnNoModalityWhenDataSourceDoesNotContainUserInput() {

        assertThat(mapper.asDataPoints(responseNode).get(1).getHeader().getAcquisitionProvenance().getModality(),
                nullValue());
    }

    @Override
    public void assertThatMeasureMatches(CaloriesBurned2 testMeasure, GoogleFitTestProperties testProperties) {

        CaloriesBurned2 expectedCaloriesBurned = new CaloriesBurned2.Builder(
                KILOCALORIE.newUnitValue(testProperties.getFpValue()),
                testProperties.getEffectiveTimeFrame().get()
        )
                .build();

        assertThat(testMeasure, equalTo(expectedCaloriesBurned));
    }
}
