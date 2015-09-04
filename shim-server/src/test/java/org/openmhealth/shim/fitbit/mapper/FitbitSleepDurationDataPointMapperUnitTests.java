package org.openmhealth.shim.fitbit.mapper;

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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Chris Schaefbauer
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

        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(singletonList(responseNodeSleep));
        assertThat(dataPoints.size(),equalTo(1));

        dataPoints = mapper.asDataPoints(singletonList(responseNodeMultipleSleep));
        assertThat(dataPoints.size(),equalTo(2));

    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){

        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(singletonList(responseNodeSleep));

        SleepDuration.Builder expectedSleepDurationBuilder = new SleepDuration.Builder(new DurationUnitValue(DurationUnit.MINUTE,831));
        OffsetDateTime offsetStartDateTime = OffsetDateTime.parse("2014-07-19T11:58:00Z");
        expectedSleepDurationBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(offsetStartDateTime,new DurationUnitValue(DurationUnit.MINUTE,961)));

        SleepDuration expectedSleepDuration = expectedSleepDurationBuilder.build();

        SleepDuration body = dataPoints.get(0).getBody();
        assertThat(body,equalTo(expectedSleepDuration));


    }

}
