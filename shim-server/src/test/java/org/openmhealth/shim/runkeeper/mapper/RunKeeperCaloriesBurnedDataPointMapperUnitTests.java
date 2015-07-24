package org.openmhealth.shim.runkeeper.mapper;

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
public class RunKeeperCaloriesBurnedDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    private RunKeeperCaloriesBurnedDataPointMapper mapper = new RunKeeperCaloriesBurnedDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/runkeeper/mapper/runkeeper-fitness-activities.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(),equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointBodies(){
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        CaloriesBurned.Builder expectedCaloriesBurnedBuilder = new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE,210.796359954334));
        expectedCaloriesBurnedBuilder.setActivityName("Cycling");
        expectedCaloriesBurnedBuilder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndDuration(
                OffsetDateTime.parse("2014-10-19T13:17:27+02:00"),
                new DurationUnitValue(DurationUnit.SECOND, 4364.74158141667)));

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedCaloriesBurnedBuilder.build()));

        expectedCaloriesBurnedBuilder = new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE,747.501013824121));
        expectedCaloriesBurnedBuilder.setActivityName("Running");
        assertThat(dataPoints.get(1).getBody(),equalTo(expectedCaloriesBurnedBuilder.build()));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointHeaders(){
        List<DataPoint<CaloriesBurned>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        DataPointHeader firstTestHeader = dataPoints.get(0).getHeader();
        assertThat(firstTestHeader.getAcquisitionProvenance().getModality(),equalTo(DataPointModality.SENSED));
        assertThat(firstTestHeader.getBodySchemaId(),equalTo(CaloriesBurned.SCHEMA_ID));
        assertThat(firstTestHeader.getAcquisitionProvenance().getAdditionalProperty("external_id").get(),equalTo("/fitnessActivities/465161536"));
        assertThat(firstTestHeader.getAcquisitionProvenance().getSourceName(),equalTo(RunKeeperDataPointMapper.RESOURCE_API_SOURCE_NAME));
        DataPointHeader secondTestHeader = dataPoints.get(1).getHeader();

        assertThat(secondTestHeader.getAcquisitionProvenance().getModality(),equalTo(DataPointModality.SELF_REPORTED));
        assertThat(secondTestHeader.getBodySchemaId(),equalTo(CaloriesBurned.SCHEMA_ID));
        assertThat(secondTestHeader.getAcquisitionProvenance().getAdditionalProperty("external_id").get(),equalTo("/fitnessActivities/4928696"));
        assertThat(secondTestHeader.getAcquisitionProvenance().getSourceName(),equalTo(RunKeeperDataPointMapper.RESOURCE_API_SOURCE_NAME));


    }
}
