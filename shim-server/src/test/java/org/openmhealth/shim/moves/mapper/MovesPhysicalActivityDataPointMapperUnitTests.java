package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matchers;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Jared Sieling.
 */
public class MovesPhysicalActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MovesPhysicalActivityDataPointMapper mapper = new MovesPhysicalActivityDataPointMapper();
    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() {
        responseNode = asJsonNode("org/openmhealth/shim/moves/mapper/moves-storyline.json");
    }

    @Test
    public void asDataPointsShouldReturnEmptyListWhenResponseIsEmpty() throws IOException {
        JsonNode emptyResponseNode = asJsonNode("org/openmhealth/shim/moves/mapper/moves-storyline-empty.json");
        assertThat(mapper.asDataPoints(emptyResponseNode), is(empty()));
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(8));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0), "cycling", "2016-04-03", "17:52:00", 1500D, 309L);
        assertThatDataPointMatches(dataPoints.get(2), "walking", "2016-04-04", "17:54:51", 5844D, 3820L);
    }

    public void assertThatDataPointMatches(DataPoint<PhysicalActivity> dataPoint, String expectedActivityName,
                                           TimeFrame expectedTimeFrame, Double expectedDistance) {

        PhysicalActivity.Builder expectedMeasureBuilder = new PhysicalActivity.Builder(expectedActivityName);

        expectedMeasureBuilder.setEffectiveTimeFrame(expectedTimeFrame);

        if (expectedDistance != null) {
            expectedMeasureBuilder.setDistance(new LengthUnitValue(METER, expectedDistance));
        }

        PhysicalActivity expectedPhysicalActivity = expectedMeasureBuilder.build();

        assertThat(dataPoint.getBody(), Matchers.equalTo(expectedPhysicalActivity));
        assertThat(dataPoint.getHeader().getBodySchemaId(), Matchers.equalTo(PhysicalActivity.SCHEMA_ID));

        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                Matchers.equalTo(MovesDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }

    public void assertThatDataPointMatches(DataPoint<PhysicalActivity> dataPoint, String expectedActivityName,
                                           String expectedEffectiveDate, String expectedEffectiveStartTime, Double expectedDistance,
                                           Long expectedDurationInMs) {

        OffsetDateTime expectedEffectiveStartDateTime = OffsetDateTime
                .of(LocalDate.parse(expectedEffectiveDate), LocalTime.parse(expectedEffectiveStartTime), ZoneOffset.ofHours(-5));

        TimeFrame expectedTimeFrame;

        if (expectedDurationInMs != null) {
            DurationUnitValue expectedDuration = new DurationUnitValue(SECOND, expectedDurationInMs);

            expectedTimeFrame = new TimeFrame(
                    TimeInterval.ofStartDateTimeAndDuration(expectedEffectiveStartDateTime, expectedDuration));
        }
        else {
            expectedTimeFrame = new TimeFrame(expectedEffectiveStartDateTime);
        }

        assertThatDataPointMatches(dataPoint, expectedActivityName, expectedTimeFrame, expectedDistance);
    }

}
