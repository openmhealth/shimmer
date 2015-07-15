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

        OffsetDateTime offsetStartDateTime = OffsetDateTime.parse("2014-09-12T11:34:19Z");
        OffsetDateTime offsetEndDateTime = OffsetDateTime.parse("2014-09-12T17:22:57Z");
        sleepDurationBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(offsetStartDateTime,offsetEndDateTime));

        SleepDuration expectedSleepDuration = sleepDurationBuilder.build();
        expectedSleepDuration.setAdditionalProperty("wakeup_count",3);
        expectedSleepDuration.setAdditionalProperty("light_sleep_duration",new DurationUnitValue(DurationUnit.SECOND,18540));
        expectedSleepDuration.setAdditionalProperty("deep_sleep_duration",new DurationUnitValue(DurationUnit.SECOND,8460));
        expectedSleepDuration.setAdditionalProperty("rem_sleep_duration",new DurationUnitValue(DurationUnit.SECOND,10460));
        expectedSleepDuration.setAdditionalProperty("duration_to_sleep",
                new DurationUnitValue(DurationUnit.SECOND, 420));
        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepDuration));

        assertThat(dataPoints.get(0).getBody().getAdditionalProperties(),
                equalTo(expectedSleepDuration.getAdditionalProperties()));
        DataPointAcquisitionProvenance acquisitionProvenance = dataPoints.get(0).getHeader().getAcquisitionProvenance();
        assertThat(acquisitionProvenance.getSourceName(),equalTo(WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getModality(),equalTo(DataPointModality.SENSED));
        assertThat(acquisitionProvenance.getAdditionalProperties().get("external_id"),equalTo(16616514L));
        assertThat(acquisitionProvenance.getAdditionalProperties().get("device_name"),equalTo("Aura"));
        assertThat(dataPoints.get(0).getHeader().getBodySchemaId(), equalTo(SleepDuration.SCHEMA_ID));


    }



}
