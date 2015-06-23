package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.StepCount;
import org.openmhealth.schema.domain.omh.TimeInterval;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Chris Schaefbauer
 */
public class FitbitStepCountDataPointUnitTests extends DataPointMapperUnitTests {

    JsonNode responseNodeUserInfo;
    JsonNode responseNodeStepTimeSeries;
    protected final FitbitStepCountDataPointMapper mapper = new FitbitStepCountDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-time-series-steps.json");
        responseNodeStepTimeSeries = objectMapper.readTree(resource.getInputStream());
        resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-user-info.json");
        responseNodeUserInfo = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<StepCount>> stepDataPoints = mapper.asDataPoints(Lists.newArrayList(responseNodeUserInfo, responseNodeStepTimeSeries));
        assertThat(stepDataPoints.size(),equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<StepCount>> stepDataPoints = mapper.asDataPoints(Lists.newArrayList(responseNodeUserInfo, responseNodeStepTimeSeries));
        testFitbitStepCountDataPoint(stepDataPoints.get(0),0,"2015-05-24");
        testFitbitStepCountDataPoint(stepDataPoints.get(1),2170,"2015-05-26");
        testFitbitStepCountDataPoint(stepDataPoints.get(2),3248,"2015-05-27");
    }

    public void testFitbitStepCountDataPoint(DataPoint<StepCount> dataPoint,long stepCount,String dateString){

        StepCount.Builder dataPointBuilderForTest = new StepCount.Builder(stepCount);

        OffsetDateTime startDateTime = OffsetDateTime.parse(dateString + "T" + "00:00:00-04:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        startDateTime = startDateTime.withNano(000000000);
        OffsetDateTime endDateTime = OffsetDateTime.parse(dateString + "T" + "23:59:59-04:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        endDateTime = endDateTime.withNano(999999999);

        dataPointBuilderForTest.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(startDateTime,endDateTime));
        StepCount dataPointForTest = dataPointBuilderForTest.build();

        assertThat(dataPoint.getBody(), CoreMatchers.equalTo(dataPointForTest));
        assertThat(dataPoint.getHeader().getBodySchemaId(), CoreMatchers.equalTo(StepCount.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"), CoreMatchers.nullValue());
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(), CoreMatchers.equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));

    }

}
