package org.openmhealth.shim.microsoft.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.MILE;
import static org.openmhealth.shim.microsoft.mapper.MicrosoftDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Wallace Wadge
 */
public class MicrosoftPhysicalActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final MicrosoftPhysicalActivityDataPointMapper mapper = new MicrosoftPhysicalActivityDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/microsoft/mapper/microsoft-physicalactivity.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(responseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        TimeInterval effectiveTimeInterval = TimeInterval.ofStartDateTimeAndDuration(
                OffsetDateTime.of(2016, 9, 14, 0, 0, 0, 0, UTC),
                new DurationUnitValue(DAY, 1));

        PhysicalActivity physicalActivity = new PhysicalActivity.Builder("Bike")
                .setDistance(new LengthUnitValue(MILE, 50))
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .setCaloriesBurned(new KcalUnitValue(KILOCALORIE, 6))
                .build();

        DataPoint<PhysicalActivity> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(physicalActivity));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
    }

}