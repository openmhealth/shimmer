package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.PhysicalActivity;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.PhysicalActivity.SCHEMA_ID;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitPhysicalActivityDataPointMapperUnitTests
        extends GoogleFitDataPointMapperUnitTests<PhysicalActivity> {

    private final GoogleFitPhysicalActivityDataPointMapper mapper = new GoogleFitPhysicalActivityDataPointMapper();

    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-activity-segments.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0),
                createStringTestProperties("Walking", "2015-01-01T22:21:57Z", "2015-01-01T23:29:49Z",
                        "derived:com.google.activity.segment:com.strava:session_activity_segment", SCHEMA_ID));

        assertThatDataPointMatches(dataPoints.get(1),
                createStringTestProperties("Aerobics", "2015-01-05T00:27:29.151Z", "2015-01-05T00:30:41.151Z",
                        "derived:com.google.activity.segment:com.mapmyrun.android2:session_activity_segment",
                        SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        assertThat(mapper.asDataPoints(responseNode).get(2).getHeader().getAcquisitionProvenance().getModality(),
                equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnNoModalityWhenDataSourceDoesNotContainUserInput() {

        assertThat(mapper.asDataPoints(responseNode).get(1).getHeader().getAcquisitionProvenance().getModality(),
                nullValue());
    }

    @Test
    public void asDataPointsShouldNotReturnDataPointsForSleepActivityType() {

        JsonNode sleepActivityNode =
                asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-activity-segments-only-sleep.json");

        assertThat(mapper.asDataPoints(sleepActivityNode), is(empty()));
    }

    @Test
    public void asDataPointsShouldNotReturnDataPointsForStationaryActivityTypes() throws IOException {

        JsonNode stationaryActivityNode = asJsonNode(
                "org/openmhealth/shim/googlefit/mapper/googlefit-merge-activity-segments-only-stationary-activity" +
                        ".json");

        assertThat(mapper.asDataPoints(stationaryActivityNode), is(empty()));
    }

    @Override
    public void assertThatMeasureMatches(PhysicalActivity testMeasure, GoogleFitTestProperties testProperties) {

        PhysicalActivity.Builder physicalActivityBuilder =
                new PhysicalActivity.Builder(testProperties.getStringValue());

        testProperties.getEffectiveTimeFrame().ifPresent(physicalActivityBuilder::setEffectiveTimeFrame);

        assertThat(testMeasure, equalTo(physicalActivityBuilder.build()));
    }
}
