package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;


/**
 * @author Chris Schaefbauer
 */
public class FitbitBodyWeightDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final FitbitBodyWeightDataPointMapper mapper = new FitbitBodyWeightDataPointMapper();
    private JsonNode responseNode;


    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-body-log-weight.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(4));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0), 56.7, "2015-05-13T18:28:59Z", 1431541739000L);
        assertThatDataPointMatches(dataPoints.get(1), 55.9, "2015-05-14T11:51:57Z", 1431604317000L);
        assertThatDataPointMatches(dataPoints.get(2), 58.1, "2015-05-22T18:12:06Z", 1432318326000L);
        assertThatDataPointMatches(dataPoints.get(3), 57.2, "2015-05-24T15:15:25Z", 1432480525000L);
    }

    @Test
    public void asDataPointsShouldReturnEmptyListWhenResponseIsEmpty() throws IOException {

        JsonNode emptyWeightNode = objectMapper.readTree("{ \"weight\": [] }");

        assertThat(mapper.asDataPoints(emptyWeightNode), is(empty()));
    }

    public void assertThatDataPointMatches(DataPoint<BodyWeight> dataPoint, double expectedMassValue,
            String expectedEffectiveDateTime, long expectedExternalId) {

        BodyWeight expectedBodyWeight = new BodyWeight.Builder(new MassUnitValue(KILOGRAM, expectedMassValue))
                .setEffectiveTimeFrame(OffsetDateTime.parse(expectedEffectiveDateTime))
                .build();

        assertThat(dataPoint.getBody(), equalTo(expectedBodyWeight));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(BodyWeight.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                equalTo(expectedExternalId));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }
}
