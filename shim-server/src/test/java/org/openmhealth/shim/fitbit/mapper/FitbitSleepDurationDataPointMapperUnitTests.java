package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Chris Schaefbauer on 6/24/15.
 */
public class FitbitSleepDurationDataPointMapperUnitTests extends DataPointMapperUnitTests {

    JsonNode responseNodeUserInfo,responseNodeSleep,responseNodeMultipleSleep;
    protected FitbitSleepDurationDataPointMapper mapper =  new FitbitSleepDurationDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-sleep.json");
        responseNodeSleep = objectMapper.readTree(resource.getInputStream());
        resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-user-info.json");
        responseNodeUserInfo = objectMapper.readTree(resource.getInputStream());
        resource = new ClassPathResource("org/openmhealth/shim/fitbit/mapper/fitbit-get-sleep-multiple.json");
        responseNodeMultipleSleep = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(Lists.newArrayList(responseNodeUserInfo, responseNodeSleep));
        assertThat(dataPoints.size(),equalTo(1));
        dataPoints = mapper.asDataPoints(Lists.newArrayList(responseNodeUserInfo, responseNodeMultipleSleep));
        assertThat(dataPoints.size(),equalTo(2));

    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(Lists.newArrayList(responseNodeUserInfo, responseNodeSleep));

        SleepDuration.Builder expectedSleepDurationBuilder = new SleepDuration.Builder(new DurationUnitValue(DurationUnit.MINUTE,831));
        OffsetDateTime offsetStartDateTime = OffsetDateTime.parse("2014-07-19T11:58:00-04:00");
        expectedSleepDurationBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime,new DurationUnitValue(DurationUnit.MINUTE,961)));

        SleepDuration expectedSleepDuration = expectedSleepDurationBuilder.build();

        SleepDuration body = dataPoints.get(0).getBody();
        assertThat(body,equalTo(expectedSleepDuration));


    }

}
