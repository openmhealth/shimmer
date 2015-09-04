package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Chris Schaefbauer
 */
public class FitbitBodyWeightDataPointMapperUnitTests extends DataPointMapperUnitTests {

    protected JsonNode responseNodeWeight;

    private final FitbitBodyWeightDataPointMapper mapper = new FitbitBodyWeightDataPointMapper();
    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-body-weight.json");
        responseNodeWeight = objectMapper.readTree(resource.getInputStream());

    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){

        List<DataPoint<BodyWeight>> bodyWeightDataPoints = mapper.asDataPoints(singletonList(responseNodeWeight));
        assertThat(bodyWeightDataPoints.size(),equalTo(4));

    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){

        List<DataPoint<BodyWeight>> bodyWeightDataPoints = mapper.asDataPoints(singletonList(responseNodeWeight));

        testFitbitBodyWeightDataPoint(bodyWeightDataPoints.get(0),56.7,"2015-05-13T18:28:59Z",1431541739000L);
        testFitbitBodyWeightDataPoint(bodyWeightDataPoints.get(1),55.9,"2015-05-14T11:51:57Z",1431604317000L);
        testFitbitBodyWeightDataPoint(bodyWeightDataPoints.get(2),58.1,"2015-05-22T18:12:06Z",1432318326000L);
        testFitbitBodyWeightDataPoint(bodyWeightDataPoints.get(3),57.2,"2015-05-24T15:15:25Z",1432480525000L);

    }

    @Test
    public void asDataPointsShouldReturnEmptyListForEmptyArray() throws IOException {

        JsonNode emptyWeightNode = objectMapper.readTree("{\n" +
                "    \"weight\": []\n" +
                "}");
        List<DataPoint<BodyWeight>> emptyDataPoints = mapper.asDataPoints(singletonList(emptyWeightNode));
        assertThat(emptyDataPoints.isEmpty(),equalTo(true));
    }

    public void testFitbitBodyWeightDataPoint(DataPoint<BodyWeight> dataPoint,double massValue,String timeString,long logId){

        BodyWeight expectedBodyWeight = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM,massValue)).setEffectiveTimeFrame(OffsetDateTime.parse(timeString)).build();
        assertThat(dataPoint.getBody(),equalTo(expectedBodyWeight));
        assertThat(dataPoint.getHeader().getBodySchemaId(),equalTo(BodyWeight.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(logId));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));

    }

}
