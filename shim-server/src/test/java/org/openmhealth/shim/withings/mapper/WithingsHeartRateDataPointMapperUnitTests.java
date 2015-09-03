package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointModality;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public class WithingsHeartRateDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private WithingsHeartRateDataPointMapper mapper = new WithingsHeartRateDataPointMapper();
    protected JsonNode responseNode;


    @BeforeTest
    public void initializeDataPoints() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/withings/mapper/withings-body-measures.json");

        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoint() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThatDataPointMatches(dataPoints.get(0), 41.0, "2015-05-31T06:06:23Z", 366956482L, null, SENSED);
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoint() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThatDataPointMatches(dataPoints.get(2), 47.0, "2015-02-26T21:57:17Z", 321858727L,
                "a few minutes after a walk", SELF_REPORTED);
    }

    private void assertThatDataPointMatches(DataPoint<HeartRate> actualDataPoint, double expectedHeartRateValue,
            String expectedDateTimeAsString, long expectedExternalId, String expectedComment,
            DataPointModality expectedModality) {

        HeartRate.Builder expectedHeartRateBuilder = new HeartRate.Builder(expectedHeartRateValue);
        expectedHeartRateBuilder.setEffectiveTimeFrame(OffsetDateTime.parse(expectedDateTimeAsString));

        if (expectedComment != null) {
            expectedHeartRateBuilder.setUserNotes(expectedComment);
        }

        HeartRate expectedHeartRate = expectedHeartRateBuilder.build();

        assertThat(actualDataPoint.getBody(), equalTo(expectedHeartRate));
        assertThat(actualDataPoint.getHeader().getBodySchemaId(), equalTo(HeartRate.SCHEMA_ID));

        DataPointAcquisitionProvenance actualProvenance = actualDataPoint.getHeader().getAcquisitionProvenance();
        assertThat(actualProvenance.getModality(), equalTo(expectedModality));
        assertThat(actualProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(actualProvenance.getAdditionalProperties().get("external_id"), equalTo(expectedExternalId));
    }
}
