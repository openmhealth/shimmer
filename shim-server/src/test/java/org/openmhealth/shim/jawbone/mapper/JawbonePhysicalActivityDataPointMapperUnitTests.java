package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.PhysicalActivity.SelfReportedIntensity.MODERATE;
import static org.openmhealth.shim.jawbone.mapper.JawboneDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Emerson Farrugia
 */
public class JawbonePhysicalActivityDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private final JawbonePhysicalActivityDataPointMapper mapper = new JawbonePhysicalActivityDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-workouts.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(Collections.singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(Collections.singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        OffsetDateTime endDateTime = ZonedDateTime.of(2013, 11, 22, 5, 47, 0, 0, UTC)
                .withZoneSameInstant(ZoneId.of("America/Los_Angeles")).toOffsetDateTime();
        TimeInterval effectiveTimeInterval =
                TimeInterval.ofEndDateTimeAndDuration(endDateTime, new DurationUnitValue(SECOND, 2_460));

        PhysicalActivity physicalActivity = new PhysicalActivity.Builder("Run")
                .setDistance(new LengthUnitValue(METER, 5_116))
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .setReportedActivityIntensity(MODERATE)
                .build();

        DataPoint<PhysicalActivity> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(physicalActivity));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").isPresent(), equalTo(true));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").get(), equalTo("40F7_htRRnT8Vo7nRBZO1X"));
        assertThat(acquisitionProvenance.getModality(), notNullValue());
        assertThat(acquisitionProvenance.getModality(), equalTo(SENSED));
    }

    // TODO add tests for self reported data
    // TODO add tests for workout type mappings
    // TODO add tests for different time zone formats
}