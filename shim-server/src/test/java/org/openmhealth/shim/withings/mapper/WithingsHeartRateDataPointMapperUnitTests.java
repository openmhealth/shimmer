package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
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
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.*;


/**
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsHeartRateDataPointMapperUnitTests extends DataPointMapperUnitTests {

    protected JsonNode responseNode;
    private WithingsHeartRateDataPointMapper mapper = new WithingsHeartRateDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("/org/openmhealth/shim/withings/mapper/withings-body-measures.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        testHeartRateDataPoint(dataPoints.get(0),41.0,"2015-05-30T23:06:23-07:00",366956482L,null,DataPointModality.SENSED);
        testHeartRateDataPoint(dataPoints.get(1),51.0,"2015-03-02T08:28:55-08:00",323560022L,null,DataPointModality.SELF_REPORTED);
        testHeartRateDataPoint(dataPoints.get(2),47.0,"2015-02-26T13:57:17-08:00",321858727L,"a few minutes after a walk",DataPointModality.SELF_REPORTED);
    }

    private void testHeartRateDataPoint(DataPoint<HeartRate> heartRateDataPoint, double value, String dateString,
            long externalId, String userComment, DataPointModality modality) {

        HeartRate.Builder expectedHeartRateBuilder = new HeartRate.Builder(value);
        expectedHeartRateBuilder.setEffectiveTimeFrame(OffsetDateTime.parse(dateString));
        if(userComment!=null){
            expectedHeartRateBuilder.setUserNotes(userComment);
        }
        HeartRate expectedHeartRate = expectedHeartRateBuilder.build();
        assertThat(heartRateDataPoint.getBody(),equalTo(expectedHeartRate));
        assertThat(heartRateDataPoint.getHeader().getBodySchemaId(),equalTo(HeartRate.SCHEMA_ID));
        assertThat(heartRateDataPoint.getHeader().getAcquisitionProvenance().getModality(),equalTo(modality));
        assertThat(heartRateDataPoint.getHeader().getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(heartRateDataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(externalId));

    }
}
