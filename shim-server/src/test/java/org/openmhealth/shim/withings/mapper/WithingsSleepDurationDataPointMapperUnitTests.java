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


/**
 * Created by Chris Schaefbauer on 7/6/15.
 */
public class WithingsSleepDurationDataPointMapperUnitTests extends DataPointMapperUnitTests {

    JsonNode responseNode;
    private WithingsSleepDurationDataPointMapper mapper = new WithingsSleepDurationDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/withings/mapper/withings-sleep-summary.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        SleepDuration.Builder sleepDurationBuilder = new SleepDuration.Builder(new DurationUnitValue(DurationUnit.SECOND,37460));

        OffsetDateTime offsetStartDateTime = OffsetDateTime.parse("2014-09-12T13:34:19+02:00");
        OffsetDateTime offsetEndDateTime = OffsetDateTime.parse("2014-09-12T19:22:57+02:00");
        sleepDurationBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(offsetStartDateTime,offsetEndDateTime));

        SleepDuration expectedSleepDuration = sleepDurationBuilder.build();
        expectedSleepDuration.setAdditionalProperty("wakeup_count",3);
        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepDuration));
        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getSourceName(),equalTo(WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME));
        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getModality(),equalTo(DataPointModality.SENSED));
        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(16616514L));
        assertThat(dataPoints.get(0).getHeader().getBodySchemaId(), equalTo(SleepDuration.SCHEMA_ID));
    }



}
