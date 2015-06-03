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
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.PhysicalActivity.SelfReportedIntensity.LIGHT;
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
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(Collections.singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), greaterThan(0));

        OffsetDateTime endDateTime = ZonedDateTime.of(2014, 8, 18, 15, 39, 44, 0, UTC)
                .withZoneSameInstant(ZoneId.of("America/Los_Angeles")).toOffsetDateTime();
        TimeInterval effectiveTimeInterval =
                TimeInterval.ofEndDateTimeAndDuration(endDateTime, new DurationUnitValue(SECOND, 300));

        PhysicalActivity physicalActivity = new PhysicalActivity.Builder("Walk")
                .setDistance(new LengthUnitValue(METER, 2_500))
                .setEffectiveTimeFrame(effectiveTimeInterval)
                .setReportedActivityIntensity(LIGHT)
                .build();

        DataPoint<PhysicalActivity> firstDataPoint = dataPoints.get(0);

        assertThat(firstDataPoint.getBody(), equalTo(physicalActivity));

        DataPointAcquisitionProvenance acquisitionProvenance = firstDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance, notNullValue());
        assertThat(acquisitionProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").isPresent(), equalTo(true));
        assertThat(acquisitionProvenance.getAdditionalProperty("external_id").get(), equalTo("UDZ763h_uKw-OTw34D0Chw"));
    }

    // TODO add tests for sensed data
    // TODO add tests for workout type mappings
    // TODO add tests for different time zone formats
}