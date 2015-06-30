package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.*;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.*;


/**
 * Created by Chris Schaefbauer on 6/29/15.
 */
public class WithingsBodyWeightDataPointMapperUnitTests extends DataPointMapperUnitTests {

    WithingsBodyWeightDataPointMapper mapper = new WithingsBodyWeightDataPointMapper();
    JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/withings/mapper/withings-body-measures.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<BodyWeight>> dataPointList = mapper.asDataPoints(Collections.singletonList(responseNode));
        assertThat(dataPointList.size(),equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<BodyWeight>> dataPointList = mapper.asDataPoints(Collections.singletonList(responseNode));

        testDataPoint(dataPointList.get(0),74.126,"2015-05-30T23:06:23-07:00",366956482L);
        testDataPoint(dataPointList.get(1),74.128,"2015-04-20T10:13:56-07:00",347186704L);

    }

    public void testDataPoint(DataPoint<BodyWeight> testDataPoint, double massValue, String offsetTimeString, long externalId){
        BodyWeight.Builder bodyWeightExpectedMeasureBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, massValue));
        bodyWeightExpectedMeasureBuilder.setEffectiveTimeFrame(OffsetDateTime.parse(offsetTimeString));
        BodyWeight bodyWeightExpected = bodyWeightExpectedMeasureBuilder.build();

        assertThat(testDataPoint.getBody(),equalTo(bodyWeightExpected));

        DataPointAcquisitionProvenance testProvenance = testDataPoint.getHeader().getAcquisitionProvenance();
        assertThat(testProvenance.getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testProvenance.getModality(),equalTo(SENSED));
        Long expectedExternalId = (Long)testDataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties()
                .get("external_id");
        assertThat(expectedExternalId,equalTo(externalId));
        assertThat(testDataPoint.getHeader().getBodySchemaId(),equalTo(BodyWeight.SCHEMA_ID));
    }
}
