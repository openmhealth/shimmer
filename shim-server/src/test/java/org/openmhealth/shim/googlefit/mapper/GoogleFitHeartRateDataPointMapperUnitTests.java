package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.HeartRate;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.HeartRate.SCHEMA_ID;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitHeartRateDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<HeartRate> {

    private final GoogleFitHeartRateDataPointMapper mapper = new GoogleFitHeartRateDataPointMapper();

    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-heart-rate.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForSingleTimePoint() {

        assertThatDataPointMatches(mapper.asDataPoints(responseNode).get(0),
                createFloatingPointTestProperties(54, "2015-01-30T15:37:48.186Z", null,
                        "raw:com.google.heart_rate.bpm:com.azumio.instantheartrate.full:", SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForTimeRange() {

        assertThatDataPointMatches(mapper.asDataPoints(responseNode).get(1),
                createFloatingPointTestProperties(58.0, "2015-07-10T14:34:32.914Z", "2015-07-10T14:34:33.915Z",
                        "raw:com.google.heart_rate.bpm:si.modula.android.instantheartrate:", SCHEMA_ID));
    }

    @Override
    public void assertThatMeasureMatches(HeartRate testMeasure, GoogleFitTestProperties testProperties) {

        HeartRate.Builder expectedHeartRateBuilder = new HeartRate.Builder(testProperties.getFpValue());

        testProperties.getEffectiveTimeFrame().ifPresent(expectedHeartRateBuilder::setEffectiveTimeFrame);

        assertThat(testMeasure, equalTo(expectedHeartRateBuilder.build()));
    }
}
