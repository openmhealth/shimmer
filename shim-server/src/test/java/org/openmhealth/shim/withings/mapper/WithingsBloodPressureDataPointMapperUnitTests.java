package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
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
import static org.openmhealth.schema.domain.omh.DataPointModality.*;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsBloodPressureDataPointMapperUnitTests extends DataPointMapperUnitTests {

    protected JsonNode responseNode;
    private WithingsBloodPressureDataPointMapper mapper = new WithingsBloodPressureDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/withings/mapper/withings-body-measures.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<BloodPressure>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<BloodPressure>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BloodPressure.Builder expectedBloodPressureBuilder = new BloodPressure.Builder(new SystolicBloodPressure(
                BloodPressureUnit.MM_OF_MERCURY,104.0), new DiastolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY,68.0));
        expectedBloodPressureBuilder.setEffectiveTimeFrame(OffsetDateTime.parse("2015-05-30T23:06:23-07:00"));
        BloodPressure expectedBloodPressure = expectedBloodPressureBuilder.build();
        assertThat(dataPoints.get(0).getBody(),equalTo(expectedBloodPressure));

        DataPointHeader testDataPointHeader = dataPoints.get(0).getHeader();
        assertThat(testDataPointHeader.getBodySchemaId(),equalTo(BloodPressure.SCHEMA_ID));
        assertThat(testDataPointHeader.getAcquisitionProvenance().getModality(),equalTo(SENSED));
        assertThat(testDataPointHeader.getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testDataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(366956482L));

    }

}
