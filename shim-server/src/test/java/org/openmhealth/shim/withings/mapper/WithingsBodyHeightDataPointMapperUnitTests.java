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
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.*;


/**
 * Created by Chris Schaefbauer on 6/30/15.
 */
public class WithingsBodyHeightDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    protected WithingsBodyHeightDataPointMapper mapper =  new WithingsBodyHeightDataPointMapper();


    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/withings/mapper/withings-body-measures.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        BodyHeight.Builder expectedBodyHeightBuilder = new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER,1.93));
        expectedBodyHeightBuilder.setEffectiveTimeFrame(OffsetDateTime.parse("2015-02-23T11:24:49-08:00"));
        BodyHeight expectedBodyHeight = expectedBodyHeightBuilder.build();
        assertThat(dataPoints.get(0).getBody(),equalTo(expectedBodyHeight));
        DataPointHeader testDataPointHeader = dataPoints.get(0).getHeader();
        assertThat(testDataPointHeader.getAcquisitionProvenance().getModality(),equalTo(SELF_REPORTED));
        assertThat(testDataPointHeader.getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testDataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(320419189L));
        assertThat(testDataPointHeader.getBodySchemaId(),equalTo(BodyHeight.SCHEMA_ID));

    }
}
