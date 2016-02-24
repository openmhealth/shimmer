package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.MILE;
import static org.openmhealth.shim.misfit.mapper.MisfitDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Emerson Farrugia
 */
public class MisfitPhysicalActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MisfitPhysicalActivityDataPointMapper mapper = new MisfitPhysicalActivityDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/misfit/mapper/misfit-sessions.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndDuration(
                OffsetDateTime.of(2015, 4, 13, 11, 46, 0, 0, ZoneOffset.ofHours(-7)),
                new DurationUnitValue(SECOND, 1140.0));

        PhysicalActivity physicalActivity = new PhysicalActivity.Builder("Walking")
                .setDistance(new LengthUnitValue(MILE, 0.9371))
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .setCaloriesBurned(new KcalUnitValue(KILOCALORIE, 96.8))
                .build();

        DataPoint<PhysicalActivity> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(physicalActivity));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").isPresent(), equalTo(true));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").get(),
                equalTo("552eab896c59ae1f7300003e"));
    }

    @Test
    public void asDataPointsShouldReturnPhysicalActivityWithoutCaloriesBurnedWhenCaloriesMissing() {

        assertThat(mapper.asDataPoints(responseNode).get(1).getBody().getCaloriesBurned(), nullValue());
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfEmptyResponse() throws IOException {

        JsonNode emptyNode = objectMapper.readTree("{\n" +
                "    \"sessions\": []\n" +
                "}");

        assertThat(mapper.asDataPoints(emptyNode), empty());
    }
}