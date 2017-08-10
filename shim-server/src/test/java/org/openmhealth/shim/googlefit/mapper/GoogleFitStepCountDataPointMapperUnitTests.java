package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount1;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.StepCount1.SCHEMA_ID;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitStepCountDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<StepCount1> {

    private final GoogleFitStepCountDataPointMapper mapper = new GoogleFitStepCountDataPointMapper();

    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-step-deltas.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<StepCount1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThatDataPointMatches(dataPoints.get(0),
                createIntegerTestProperties(4146, "2015-02-02T22:49:39.811Z", "2015-02-02T23:25:20.811Z",
                        "derived:com.google.step_count.delta:com.nike.plusgps:", SCHEMA_ID));

        assertThatDataPointMatches(dataPoints.get(1),
                createIntegerTestProperties(17, "2015-07-10T21:58:17.687316406Z", "2015-07-10T21:59:17.687316406Z",
                        "derived:com.google.step_count.cumulative:com.google.android.gms:samsung:Galaxy " +
                                "Nexus:32b1bd9e:soft_step_counter", SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        List<DataPoint<StepCount1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints.get(2).getHeader().getAcquisitionProvenance().getModality(),
                equalTo(SELF_REPORTED));

        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getModality(), nullValue());

    }

    @Test
    public void asDataPointsShouldReturnZeroDataPointsWithEmptyData() {

        JsonNode emptyNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-empty.json");

        assertThat(mapper.asDataPoints(emptyNode), is(empty()));
    }

    /* Helper methods */

    @Override
    public void assertThatMeasureMatches(StepCount1 testMeasure, GoogleFitTestProperties testProperties) {

        StepCount1.Builder expectedStepCountBuilder = new StepCount1.Builder(testProperties.getIntValue());

        setExpectedEffectiveTimeFrame(expectedStepCountBuilder, testProperties);

        assertThat(testMeasure, equalTo(expectedStepCountBuilder.build()));
    }
}
