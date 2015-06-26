package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.BodyMassIndex;
import org.openmhealth.schema.domain.omh.BodyMassIndexUnit;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.TypedUnitValue;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Chris Schaefbauer
 */
public class FitbitBodyMassIndexDataPointMapperUnitTests extends DataPointMapperUnitTests{

    JsonNode responseNodeWeight;
    JsonNode responseNodeUserInfo;
    private final FitbitBodyMassIndexDataPointMapper mapper = new FitbitBodyMassIndexDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-body-weight.json");
        responseNodeWeight = objectMapper.readTree(resource.getInputStream());
        resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-user-info.json");
        responseNodeUserInfo = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(Lists.newArrayList(responseNodeUserInfo,responseNodeWeight));
        assertThat(dataPoints.size(),equalTo(4));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(Lists.newArrayList(responseNodeUserInfo, responseNodeWeight));
        testFitbitBodyMassIndexDataPoint(dataPoints.get(0), 21.48, "2015-05-13T18:28:59-04:00", 1431541739000L);
        testFitbitBodyMassIndexDataPoint(dataPoints.get(1), 21.17, "2015-05-14T11:51:57-04:00", 1431604317000L);
        testFitbitBodyMassIndexDataPoint(dataPoints.get(2), 21.99, "2015-05-22T18:12:06-04:00", 1432318326000L);
        testFitbitBodyMassIndexDataPoint(dataPoints.get(3), 21.65, "2015-05-24T15:15:25-04:00", 1432480525000L);

    }

    public void testFitbitBodyMassIndexDataPoint(DataPoint<BodyMassIndex> dataPoint, double bodyMassIndexValue, String timeString, long logId){

        TypedUnitValue<BodyMassIndexUnit> bmiValue = new TypedUnitValue<BodyMassIndexUnit>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER,bodyMassIndexValue);
        BodyMassIndex expectedBodyMassIndex = new BodyMassIndex.Builder(bmiValue).setEffectiveTimeFrame(OffsetDateTime.parse(timeString)).build();
        assertThat(dataPoint.getBody(), equalTo(expectedBodyMassIndex));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(BodyMassIndex.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"), equalTo(logId));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(), equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));

    }
}
