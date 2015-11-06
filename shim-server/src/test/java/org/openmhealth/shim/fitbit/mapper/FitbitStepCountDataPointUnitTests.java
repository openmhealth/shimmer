package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnit;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount;
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
public class FitbitStepCountDataPointUnitTests extends DataPointMapperUnitTests {

    private final FitbitStepCountDataPointMapper mapper = new FitbitStepCountDataPointMapper();
    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-time-series-steps.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<StepCount>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThatDataPointMatches(dataPoints.get(0), 2170, "2015-05-26");
        assertThatDataPointMatches(dataPoints.get(1), 3248, "2015-05-27");
    }

    public void assertThatDataPointMatches(DataPoint<StepCount> dataPoint, long expectedStepCountValue,
            String expectedEffectiveDate) {

        StepCount expectedStepCount = new StepCount.Builder(expectedStepCountValue)
                .setEffectiveTimeFrame(ofStartDateTimeAndDuration(
                        OffsetDateTime.of(LocalDate.parse(expectedEffectiveDate).atStartOfDay(), UTC),
                        new DurationUnitValue(DurationUnit.DAY, 1)))
                .build();

        assertThat(dataPoint.getBody(), equalTo(expectedStepCount));
        assertThat(dataPoint.getHeader().getBodySchemaId(), equalTo(StepCount.SCHEMA_ID));
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
