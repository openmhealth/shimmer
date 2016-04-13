package org.openmhealth.shim.jawbone.mapper;

import com.beust.jcommander.internal.Maps;
import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matchers;
import org.openmhealth.schema.domain.omh.*;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.KcalUnit.KILOCALORIE;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.PhysicalActivity.SelfReportedIntensity.MODERATE;
import static org.openmhealth.shim.jawbone.mapper.JawboneDataPointMapper.RESOURCE_API_SOURCE_NAME;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * @author Emerson Farrugia
 */
public class JawbonePhysicalActivityDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<PhysicalActivity> {

    private final JawbonePhysicalActivityDataPointMapper mapper = new JawbonePhysicalActivityDataPointMapper();

    private JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource = new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-workouts.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
        initializeEmptyNode();
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWithEmptyResponse() {

        testEmptyNode(mapper);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

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
                .setCaloriesBurned(new KcalUnitValue(KILOCALORIE, 634.928678924))
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

    @Test
    public void asDataPointsShouldReturnCorrectMissingSensedDataPoints() {

        List<DataPoint<PhysicalActivity>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        PhysicalActivity expectedPhysicalActivity = new PhysicalActivity.Builder("Bike")
                .setDistance(new LengthUnitValue(METER, 6318.2688961))
                .setEffectiveTimeFrame(
                        TimeInterval.ofEndDateTimeAndDuration(OffsetDateTime.parse("2015-04-29T16:07:07-04:00"),
                                new DurationUnitValue(SECOND, 343)))
                .setCaloriesBurned(new KcalUnitValue(KILOCALORIE, 27.16863765916))
                .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedPhysicalActivity));

        DataPointHeader testDataPointHeader = dataPoints.get(1).getHeader();
        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "SbiOBJjJJk8n2xLpNTMFng12pGRjX-qe");
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-04-29T20:07:56Z");
        testProperties.put(HEADER_SCHEMA_ID_KEY, PhysicalActivity.SCHEMA_ID);
        testDataPointHeader(testDataPointHeader, testProperties);
    }

    @Test
    public void asDataPointsShouldReturnDataPointWithoutCaloriesBurnedWhenCaloriesAreMissing() {

        assertThat(mapper.asDataPoints(responseNode).get(2).getBody().getCaloriesBurned(), nullValue());
    }

    // TODO multiple tests?
    @Test
    public void asSelfReportedIntensityShouldReturnCorrectIntensityWhenWithinSpecification() {
        PhysicalActivity.SelfReportedIntensity selfReportedIntensity = mapper.asSelfReportedIntensity(1);
        assertThat(selfReportedIntensity, Matchers.equalTo(PhysicalActivity.SelfReportedIntensity.LIGHT));

        selfReportedIntensity = mapper.asSelfReportedIntensity(2);
        assertThat(selfReportedIntensity, Matchers.equalTo(PhysicalActivity.SelfReportedIntensity.MODERATE));

        selfReportedIntensity = mapper.asSelfReportedIntensity(3);
        assertThat(selfReportedIntensity, Matchers.equalTo(PhysicalActivity.SelfReportedIntensity.MODERATE));

        selfReportedIntensity = mapper.asSelfReportedIntensity(4);
        assertThat(selfReportedIntensity, Matchers.equalTo(PhysicalActivity.SelfReportedIntensity.VIGOROUS));

        selfReportedIntensity = mapper.asSelfReportedIntensity(5);
        assertThat(selfReportedIntensity, Matchers.equalTo(PhysicalActivity.SelfReportedIntensity.VIGOROUS));

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void asSelfReportedIntensityShouldThrowExceptionWhenOutsideSpecification() {

        mapper.asSelfReportedIntensity(6);
    }

    // TODO experiment with formatting and quote replacement
    @Test
    public void isSensedShouldReturnTrueWhenStepsArePresent() throws IOException {

        JsonNode nodeWithSteps = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"steps\": 5128,\n" +
                "\"time\": 2460\n" +
                "}\n" +
                "}");
        assertTrue(mapper.isSensed(nodeWithSteps));
    }

    @Test
    public void isSensedShouldReturnFalseWhenStepsAreNotPresent() throws IOException {

        JsonNode nodeWithSteps = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"steps\": 0,\n" +
                "\"time\": 2460\n" +
                "}\n" +
                "}");
        assertFalse(mapper.isSensed(nodeWithSteps));

        nodeWithSteps = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"time\": 2460\n" +
                "}\n" +
                "}");
        assertFalse(mapper.isSensed(nodeWithSteps));
    }

    @Test
    public void getActivityNameShouldReturnTitleWhenPresent() {

        String activityName = mapper.getActivityName("run", 15);
        assertThat(activityName, equalTo("run"));

    }

    @Test
    public void getActivityNameShouldReturnCorrectSubtypeWhenTitleIsMissing() {

        String activityName = mapper.getActivityName(null, 9);
        assertThat(activityName, equalTo("crossfit"));

        activityName = mapper.getActivityName(null, 29);
        assertThat(activityName, equalTo("workout"));

    }

    @Test
    public void getActivityNameShouldReturnGenericWorkoutNameWhenSubtypeAndTitleAreMissing() {

        String activityName = mapper.getActivityName(null, null);
        assertThat(activityName, equalTo("workout"));
    }

}