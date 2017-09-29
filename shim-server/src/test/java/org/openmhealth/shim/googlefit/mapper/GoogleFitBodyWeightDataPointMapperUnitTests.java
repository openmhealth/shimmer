package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.openmhealth.schema.domain.omh.BodyWeight.*;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;


/**
 * @author Chris Schaefbauer
 */
public class GoogleFitBodyWeightDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<BodyWeight> {

    private final GoogleFitBodyWeightDataPointMapper mapper = new GoogleFitBodyWeightDataPointMapper();

    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-weight.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForSingleTimePoint() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0),
                createFloatingPointTestProperties(72.0999984741211, "2015-02-13T00:00:00Z", null,
                        "raw:com.google.weight:com.fatsecret.android:", SCHEMA_ID));

        assertThatDataPointMatches(dataPoints.get(1),
                createFloatingPointTestProperties(72, "2015-02-17T16:57:13.313Z", null,
                        "raw:com.google.weight:com.wsl.noom:", SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWithTimeRange() {

        assertThatDataPointMatches(mapper.asDataPoints(responseNode).get(2),
                createFloatingPointTestProperties(75.75070190429688, "2015-07-08T03:17:00Z", "2015-07-08T03:17:10.020Z",
                        "raw:com.google.weight:com.google.android.apps.fitness:user_input", SCHEMA_ID));
    }

    @Test
    public void asDataPointsShouldReturnSelfReportedAsModalityWhenDataSourceContainsUserInput() {

        assertThat(mapper.asDataPoints(responseNode).get(2).getHeader().getAcquisitionProvenance().getModality(),
                equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnNoModalityWhenDataSourceDoesNotContainUserInput() {

        assertThat(mapper.asDataPoints(responseNode).get(0).getHeader().getAcquisitionProvenance().getModality(),
                nullValue());
    }

    @Override
    public void assertThatMeasureMatches(BodyWeight testMeasure, GoogleFitTestProperties testProperties) {

        BodyWeight.Builder expectedBodyWeightBuilder =
                new BodyWeight.Builder(new MassUnitValue(KILOGRAM, testProperties.getFpValue()));

        testProperties.getEffectiveTimeFrame().ifPresent(expectedBodyWeightBuilder::setEffectiveTimeFrame);

        assertThat(testMeasure, equalTo(expectedBodyWeightBuilder.build()));
    }
}
