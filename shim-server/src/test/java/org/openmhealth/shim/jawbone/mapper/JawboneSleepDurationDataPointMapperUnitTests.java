package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.*;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Chris Schaefbauer
 */
public class JawboneSleepDurationDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<SleepDuration1> {

    JawboneSleepDurationDataPointMapper mapper = new JawboneSleepDurationDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-sleeps.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
        initializeEmptyNode();
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWithEmptyResponse() {

        testEmptyNode(mapper);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<SleepDuration1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenWakeUpCountEqualZeroAndShared() {

        List<DataPoint<SleepDuration1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        SleepDuration1 expectedSleepDuration = new SleepDuration1
                .Builder(new DurationUnitValue(DurationUnit.SECOND, 10356))
                .setEffectiveTimeFrame(
                        TimeInterval.ofStartDateTimeAndEndDateTime(OffsetDateTime.parse("2015-08-04T22:48:51-06:00"),
                                OffsetDateTime.parse("2015-08-05T01:58:35-06:00")))
                .build();

        expectedSleepDuration.setAdditionalProperty("wakeup_count", 0);

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepDuration));

        Map<String, Object> testProperties = Maps.newHashMap();

        testProperties.put(HEADER_EXTERNAL_ID_KEY, "QkfTizSpRdsDKwErMhvMqG9VDhpfyDGd");
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-08-05T09:52:00Z");
        testProperties.put(HEADER_SCHEMA_ID_KEY, SleepDuration1.SCHEMA_ID);
        testProperties.put(HEADER_SHARED_KEY, true);
        testProperties.put(HEADER_SENSED_KEY, DataPointModality.SENSED);

        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWhenWakeUpCountGreaterThanZeroAndNotShared() {

        List<DataPoint<SleepDuration1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        SleepDuration1 expectedSleepDuration =
                new SleepDuration1.Builder(new DurationUnitValue(DurationUnit.SECOND, 27900)).setEffectiveTimeFrame(
                        TimeInterval.ofStartDateTimeAndEndDateTime(OffsetDateTime.parse("2015-08-03T23:05:00-04:00"),
                                OffsetDateTime.parse("2015-08-04T07:15:00-04:00"))).build();
        expectedSleepDuration.setAdditionalProperty("wakeup_count", 2);

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedSleepDuration));

        Map<String, Object> testProperties = Maps.newHashMap();

        testProperties.put(HEADER_SHARED_KEY, false);
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "QkfTizSpRdvIs6MMJbKP6ulqeYwu5c2v");
        testProperties.put(HEADER_SCHEMA_ID_KEY, SleepDuration1.SCHEMA_ID);
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-08-04T12:10:56Z");
        testProperties.put(HEADER_SENSED_KEY, DataPointModality.SENSED);

        testDataPointHeader(dataPoints.get(1).getHeader(), testProperties);
    }

    @Test
    public void isSensedShouldReturnTrueWhenAwakePropertyExistsAndGreaterThanZero() throws IOException {

        JsonNode testNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"awake\": 100,\n" +
                "\"light\": 0\n" +
                "},\n" +
                "\"time_created\": 1439990403\n" +
                "}");
        assertThat(mapper.isSensed(testNode), equalTo(true));
    }

    @Test
    public void isSensedShouldReturnTrueWhenLightPropertyExistsAndGreaterThanZero() throws IOException {

        JsonNode testNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"awake\": 0,\n" +
                "\"light\": 100\n" +
                "},\n" +
                "\"time_created\": 1439990403\n" +
                "}");
        assertThat(mapper.isSensed(testNode), equalTo(true));
    }

    @Test
    public void isSensedShouldReturnTrueWhenLightAndAwakePropertiesExistsAndGreaterThanZero() throws IOException {

        JsonNode testNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"awake\": 100,\n" +
                "\"light\": 100\n" +
                "},\n" +
                "\"time_created\": 1439990403\n" +
                "}");
        assertThat(mapper.isSensed(testNode), equalTo(true));
    }

    @Test
    public void isSensedShouldReturnFalseWhenLightAndAwakePropertiesAreEqualToZero() throws IOException {

        JsonNode testNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"awake\": 0,\n" +
                "\"light\": 0\n" +
                "},\n" +
                "\"time_created\": 1439990403\n" +
                "}");
        assertThat(mapper.isSensed(testNode), equalTo(false));
    }

    @Test
    public void isSensedShouldReturnFalseWhenLightOrAwakePropertiesAreMissing() throws IOException {

        JsonNode testNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"awake\": 0\n" +
                "},\n" +
                "\"time_created\": 1439990403\n" +
                "}");
        assertThat(mapper.isSensed(testNode), equalTo(false));

        testNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"light\": 0\n" +
                "},\n" +
                "\"time_created\": 1439990403\n" +
                "}");
        assertThat(mapper.isSensed(testNode), equalTo(false));
    }
}
