package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointModality;
import org.openmhealth.schema.domain.omh.PhysicalActivity;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitPhysicalActivityDataPointMapperUnitTests
        extends GoogleFitDataPointMapperUnitTests<PhysicalActivity> {

    private GoogleFitPhysicalActivityDataPointMapper mapper = new GoogleFitPhysicalActivityDataPointMapper();
    protected JsonNode sleepActivityNode;

    @BeforeTest
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-activity-segments.json");
        sleepActivityNode =
                asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-activity-segments-only-sleep.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testGoogleFitDataPoint(dataPoints.get(0),
                createStringTestProperties("Walking", "2015-01-01T22:21:57Z", "2015-01-01T23:29:49Z",
                        "derived:com.google.activity.segment:com.strava:session_activity_segment"));
        testGoogleFitDataPoint(dataPoints.get(1),
                createStringTestProperties("Aerobics", "2015-01-05T00:27:29.151Z", "2015-01-05T00:30:41.151Z",
                        "derived:com.google.activity.segment:com.mapmyrun.android2:session_activity_segment"));

    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints.get(2).getHeader().getAcquisitionProvenance().getModality(),
                equalTo(DataPointModality.SELF_REPORTED));

        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(), nullValue());

    }

    @Test
    public void asDataPointsShouldNotReturnDataPointsForSleepActivityType() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(sleepActivityNode));
        assertThat(dataPoints.size(), equalTo(0));
    }

    @Test
    public void asDataPointsShouldNotReturnDataPointsForStationaryActivityTypes() throws IOException {

        JsonNode stationaryActivityNode = asJsonNode(
                "org/openmhealth/shim/googlefit/mapper/googlefit-merge-activity-segments-only-stationary-activity" +
                        ".json");

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(stationaryActivityNode));
        assertThat(dataPoints.size(), equalTo(0));
    }

   /* Helper methods */

    @Override
    public void testGoogleFitMeasureFromDataPoint(PhysicalActivity testMeasure, Map<String, Object> properties) {

        PhysicalActivity.Builder physicalActivityBuilder =
                new PhysicalActivity.Builder((String) properties.get("stringValue"));
        setExpectedEffectiveTimeFrame(physicalActivityBuilder, properties);
        PhysicalActivity expectedPhysicalActivity = physicalActivityBuilder.build();

        assertThat(testMeasure, equalTo(expectedPhysicalActivity));
    }
}
