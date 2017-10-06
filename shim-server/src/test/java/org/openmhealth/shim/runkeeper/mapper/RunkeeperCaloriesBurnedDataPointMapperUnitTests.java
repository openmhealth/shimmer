package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.CaloriesBurned2;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.TimeInterval;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.CaloriesBurned2.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.runkeeper.mapper.RunkeeperDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public class RunkeeperCaloriesBurnedDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    private final RunkeeperCaloriesBurnedDataPointMapper mapper = new RunkeeperCaloriesBurnedDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/runkeeper/mapper/runkeeper-fitness-activities.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointBodies() {

        List<DataPoint<CaloriesBurned2>> dataPoints = mapper.asDataPoints(responseNode);

        TimeInterval effectiveTimeInterval = ofStartDateTimeAndDuration(
                OffsetDateTime.parse("2014-10-19T13:17:27+02:00"),
                SECOND.newUnitValue(4364.74158141667));

        CaloriesBurned2 expectedCaloriesBurned =
                new CaloriesBurned2.Builder(KILOCALORIE.newUnitValue(210.796359954334), effectiveTimeInterval)
                        .setActivityName("Cycling")
                        .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedCaloriesBurned));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointHeaders() {

        DataPointHeader firstTestHeader = mapper.asDataPoints(responseNode).get(0).getHeader();

        assertThat(firstTestHeader.getAcquisitionProvenance().getModality(), equalTo(SENSED));

        assertThat(firstTestHeader.getBodySchemaId(), equalTo(SCHEMA_ID));

        assertThat(firstTestHeader.getAcquisitionProvenance().getAdditionalProperty("external_id").get(),
                equalTo("/fitnessActivities/465161536"));

        assertThat(firstTestHeader.getAcquisitionProvenance().getSourceName(),
                equalTo(RESOURCE_API_SOURCE_NAME));
    }
}
