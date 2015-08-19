package org.openmhealth.shim.jawbone.mapper;

import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.*;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Chris Schaefbauer
 */
public class JawboneSleepDurationDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<SleepDuration> {

    JawboneSleepDurationDataPointMapper mapper = new JawboneSleepDurationDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-sleeps.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(singletonList(responseNode));


        SleepDuration expectedSleepDuration = new SleepDuration
                .Builder(new DurationUnitValue(DurationUnit.SECOND,10356))
                .setEffectiveTimeFrame(
                        TimeInterval.ofStartDateTimeAndEndDateTime(OffsetDateTime.parse("2015-08-04T22:48:51-06:00"),
                                OffsetDateTime.parse("2015-08-05T01:58:35-06:00")))
                .build();
        expectedSleepDuration.setAdditionalProperty("wakeup_count", 0);
        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepDuration));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put(HEADER_EXTERNAL_ID_KEY,"QkfTizSpRdsDKwErMhvMqG9VDhpfyDGd");
        testProperties.put(HEADER_SOURCE_UPDATE_KEY,"2015-08-05T09:52:00Z");
        testProperties.put(HEADER_SCHEMA_ID_KEY,SleepDuration.SCHEMA_ID);
        testProperties.put(HEADER_SHARED_KEY,true);
        testProperties.put(HEADER_SENSED_KEY,DataPointModality.SENSED);
        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);

        expectedSleepDuration = new SleepDuration.Builder(new DurationUnitValue( DurationUnit.SECOND,27900)).setEffectiveTimeFrame(
                TimeInterval.ofStartDateTimeAndEndDateTime(OffsetDateTime.parse("2015-08-03T23:05:00-04:00"),OffsetDateTime.parse("2015-08-04T07:15:00-04:00"))).build();
        expectedSleepDuration.setAdditionalProperty("wakeup_count",2);
        assertThat(dataPoints.get(1).getBody(), equalTo(expectedSleepDuration));

        testProperties = Maps.newHashMap();
        testProperties.put(HEADER_SHARED_KEY,false);
        testProperties.put(HEADER_EXTERNAL_ID_KEY,"QkfTizSpRdvIs6MMJbKP6ulqeYwu5c2v");
        testProperties.put(HEADER_SCHEMA_ID_KEY,SleepDuration.SCHEMA_ID);
        testProperties.put(HEADER_SOURCE_UPDATE_KEY,"2015-08-04T12:10:56Z");
        testProperties.put(HEADER_SENSED_KEY,DataPointModality.SENSED);
        testDataPointHeader(dataPoints.get(1).getHeader(), testProperties);
    }

}
