package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.BodyMassIndexUnit2.KILOGRAMS_PER_SQUARE_METER;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class FitbitBodyMassIndexDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final FitbitBodyMassIndexDataPointMapper mapper = new FitbitBodyMassIndexDataPointMapper();
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

        List<DataPoint<BodyMassIndex2>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0), 21.48, "2015-05-13T18:28:59Z", 1431541739000L);
        assertThatDataPointMatches(dataPoints.get(1), 21.17, "2015-05-14T11:51:57Z", 1431604317000L);
        assertThatDataPointMatches(dataPoints.get(2), 21.99, "2015-05-22T18:12:06Z", 1432318326000L);
        assertThatDataPointMatches(dataPoints.get(3), 21.65, "2015-05-24T15:15:25Z", 1432480525000L);
    }

    public void assertThatDataPointMatches(DataPoint<BodyMassIndex2> dataPoint, double expectedBmiValue,
            String expectedEffectiveDateTime, long expectedExternalId) {

        TypedUnitValue<BodyMassIndexUnit2> bmiUnitValue =
                new TypedUnitValue<>(KILOGRAMS_PER_SQUARE_METER, expectedBmiValue);

        BodyMassIndex2 expectedBodyMassIndex =
                new BodyMassIndex2.Builder(bmiUnitValue, OffsetDateTime.parse(expectedEffectiveDateTime))
                        .build();

        assertThat(dataPoint.getBody(), equalTo(expectedBodyMassIndex));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(BodyMassIndex2.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                equalTo(expectedExternalId));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }
}
