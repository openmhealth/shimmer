package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;


/**
 * @author Chris Schaefbauer
 */
public class FitbitStepCountDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final FitbitStepCountDataPointMapper mapper = new FitbitStepCountDataPointMapper();
    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-activities-steps-timeseries.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenMultipleInResponse() {

        List<DataPoint<StepCount2>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThatDataPointMatches(dataPoints.get(0), 175, "2015-08-23");
        assertThatDataPointMatches(dataPoints.get(1), 2937, "2015-08-24");
    }

    public void assertThatDataPointMatches(DataPoint<StepCount2> dataPoint, long expectedStepCountValue,
            String expectedEffectiveDate) {

        StepCount2 expectedStepCount = new StepCount2.Builder(
                expectedStepCountValue,
                ofStartDateTimeAndDuration(
                        OffsetDateTime.of(LocalDate.parse(expectedEffectiveDate).atStartOfDay(), UTC),
                        new DurationUnitValue(DurationUnit.DAY, 1)
                ))
                .build();

        assertThat(dataPoint.getBody(), equalTo(expectedStepCount));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(StepCount2.SCHEMA_ID));
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                nullValue());
        assertThat(dataPoint.getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(FitbitDataPointMapper.RESOURCE_API_SOURCE_NAME));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListWhenStepCountEqualsZero() throws IOException {

        JsonNode zeroStepsNode = objectMapper.readTree("{\n" +
                "    \"activities-steps\": [\n" +
                "        {\n" +
                "            \"dateTime\": \"2015-05-24\",\n" +
                "            \"value\": \"0\"\n" +
                "        }\n" +
                "    ]\n" +
                "}\n");

        assertThat(mapper.asDataPoints(zeroStepsNode), is(empty()));
    }
}
