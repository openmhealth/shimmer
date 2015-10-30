package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Chris Schaefbauer
 */
public class FitbitPhysicalActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    JsonNode responseNodeSingleActivity,responseNodeMultipleActivities,responseNodeEmptyActivities;
    FitbitPhysicalActivityDataPointMapper mapper = new FitbitPhysicalActivityDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-activities-single.json");
        responseNodeSingleActivity = objectMapper.readTree(resource.getInputStream());
        resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-activities-multiple.json");
        responseNodeMultipleActivities = objectMapper.readTree(resource.getInputStream());
        resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-activities-empty.json");
        responseNodeEmptyActivities = objectMapper.readTree(resource.getInputStream());

    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNodeSingleActivity));
        assertThat(dataPoints.size(),equalTo(1));

        dataPoints = mapper.asDataPoints(singletonList(responseNodeMultipleActivities));
        assertThat(dataPoints.size(),equalTo(3));

        dataPoints = mapper.asDataPoints(singletonList(responseNodeEmptyActivities));
        assertThat(dataPoints.size(),equalTo(0));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointForSingleActivty(){
        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNodeSingleActivity));
        testFitbitPhysicalActivityDataPoint(dataPoints.get(0),"Walk","2014-06-19","09:00",3.36,3600000L,79441095L);

    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForMultipleActivities(){
        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNodeMultipleActivities));
        testFitbitPhysicalActivityDataPoint(dataPoints.get(0),"Run","2015-06-23","11:55",6.43738,1440000L,253202765L);
        testFitbitPhysicalActivityDataPoint(dataPoints.get(1),"Swimming","2015-06-23","10:00",null,null,253246706L);
        testFitbitPhysicalActivityDataPoint(dataPoints.get(2),"Walk","2015-06-23",null,6.43738,null,253202766L);

    }

    public void testFitbitPhysicalActivityDataPoint(DataPoint<PhysicalActivity> dataPoint,String activityName,String dateString,String startTimeString, Double distance,Long durationInMillis,Long logId){

        PhysicalActivity.Builder dataPointBuilderForExpected = new PhysicalActivity.Builder(activityName);

        if(startTimeString!=null && durationInMillis!=null){
            OffsetDateTime offsetStartDateTime = OffsetDateTime.parse(dateString + "T" + startTimeString + "Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            dataPointBuilderForExpected.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime,new DurationUnitValue(DurationUnit.MILLISECOND,durationInMillis)));
        }
        else if(startTimeString !=null){
            OffsetDateTime offsetStartDateTime = OffsetDateTime.parse(dateString + "T" + startTimeString + "Z",DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            dataPointBuilderForExpected.setEffectiveTimeFrame(offsetStartDateTime);
        }
        else{
            OffsetDateTime offsetStartDateTime = OffsetDateTime.parse(dateString + "T" + "00:00:00Z",DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            dataPointBuilderForExpected.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime, new DurationUnitValue(DurationUnit.DAY,1)));
        }

        if(distance!=null){
            dataPointBuilderForExpected.setDistance(new LengthUnitValue(LengthUnit.KILOMETER,distance));
        }
        PhysicalActivity expectedPhysicalActivity = dataPointBuilderForExpected.build();

        assertThat(dataPoint.getBody(), equalTo(expectedPhysicalActivity));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(PhysicalActivity.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"), equalTo(logId));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(), equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));

    }

}
