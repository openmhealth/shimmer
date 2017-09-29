package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.moves.mapper.MovesDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Jared Sieling
 * @author Emerson Farrugia
 */
public class MovesPhysicalActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MovesPhysicalActivityDataPointMapper mapper = new MovesPhysicalActivityDataPointMapper();
    private JsonNode storylineResponseNode;
    private JsonNode storylineNoSegmentsResponseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        storylineResponseNode = asJsonNode("/org/openmhealth/shim/moves/mapper/moves-user-storyline-daily.json");
        storylineNoSegmentsResponseNode =
                asJsonNode("/org/openmhealth/shim/moves/mapper/moves-user-storyline-daily-no-segments.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(storylineResponseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(7)); // activities without time frames are ignored
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfResponseHasNoSegments() throws IOException {

        assertThat(mapper.asDataPoints(storylineNoSegmentsResponseNode), empty());
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(storylineResponseNode);

        assertThatDataPointMatches(dataPoints.get(0),
                "walking",
                OffsetDateTime.parse("2012-12-12T07:14:30+02:00"),
                OffsetDateTime.parse("2012-12-12T07:27:32+02:00"),
                1251L,
                99L);
    }

    public void assertThatDataPointMatches(
            DataPoint<PhysicalActivity> dataPoint,
            String expectedActivityName,
            OffsetDateTime expectedEffectiveStartDateTime,
            OffsetDateTime expectedEffectiveEndDateTime,
            Long expectedDistanceInM,
            Long expectedCaloriesInKCal) {

        PhysicalActivity.Builder expectedMeasureBuilder = new PhysicalActivity.Builder(expectedActivityName);

        expectedMeasureBuilder.setEffectiveTimeFrame(
                ofStartDateTimeAndEndDateTime(expectedEffectiveStartDateTime, expectedEffectiveEndDateTime));

        if (expectedDistanceInM != null) {
            expectedMeasureBuilder.setDistance(new LengthUnitValue(METER, expectedDistanceInM));
        }

        if (expectedCaloriesInKCal != null) {
            expectedMeasureBuilder.setCaloriesBurned(new KcalUnitValue(KILOCALORIE, expectedCaloriesInKCal));
        }

        PhysicalActivity expectedPhysicalActivity = expectedMeasureBuilder.build();

        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(PhysicalActivity.SCHEMA_ID));
        assertThat(dataPoint.getBody(), equalTo(expectedPhysicalActivity));

        DataPointAcquisitionProvenance acquisitionProvenance = dataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
    }
}
