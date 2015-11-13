package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.openmhealth.shim.googlefit.mapper.GoogleFitDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * Base class for unit tests that evaluate individual data point mappers, used to build the measure specific unit
 * tests.
 *
 * @author Chris Schaefbauer
 */
public abstract class GoogleFitDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    protected JsonNode responseNode;

    public abstract void initializeResponseNode() throws IOException;

    /**
     * Implemented by measure specific test classes in order to test the {@link Measure} contained within the mapper
     * created {@link DataPoint}. Should contain the assertions needed to test the individual values in the measure.
     */
    public abstract void testGoogleFitMeasureFromDataPoint(T testMeasure, Map<String, Object> properties);

    /**
     * Used to test data points created through {@link Measure} specific Google Fit mappers.
     *
     * @param dataPoint datapoint created by the mapper
     * @param properties a map containing different properties to test against the mapper generated datapoint, should
     * contain keys that are used in this generic data point test as well as the mapper specific test
     */
    public void testGoogleFitDataPoint(DataPoint<T> dataPoint, Map<String, Object> properties) {

        testGoogleFitMeasureFromDataPoint(dataPoint.getBody(), properties);
        DataPointHeader dataPointHeader = dataPoint.getHeader();

        assertThat(dataPointHeader.getAcquisitionProvenance().getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));

        assertThat(dataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get(
                "source_origin_id"), equalTo(properties.get("sourceOriginId")));

        if (properties.containsKey("modality")) {
            assertThat(dataPointHeader.getAcquisitionProvenance().getModality(), equalTo(properties.get("modality")));
        }

        if (!properties.containsKey("modality")) {
            assertThat(dataPointHeader.getAcquisitionProvenance().getModality(), nullValue());
        }

    }

    /**
     * Creates the properties map used for generating an expected values datapoint to test google fit data points
     * against.
     *
     * @param fpValue a floating point value from a Google fit JSON test datapoint
     * @param startDateTime a string containing the start timestamp in unix epoch nanoseconds
     * @param endDateTime a string containing the end timestamp in unix epoch nanoseconds
     * @param sourceOriginId a string containing the origin source id from the datapoint from the JSON test data
     * @return a map with the properties needed to generate an expected datapoint to test a google fit datapoint
     */
    public Map<String, Object> createFloatingPointTestProperties(double fpValue, String startDateTime,
            String endDateTime, String sourceOriginId) {
        Map<String, Object> properties = createTestProperties(startDateTime, endDateTime, sourceOriginId);
        properties.put("fpValue", fpValue);
        return properties;
    }

    /**
     * Creates the properties map used for generating an expected values datapoint to test google fit data points
     * against.
     *
     * @param intValue an integer value from a Google fit JSON test datapoint
     * @param startDateTime a string containing the start timestamp in unix epoch nanoseconds
     * @param endDateTime a string containing the end timestamp in unix epoch nanoseconds
     * @param sourceOriginId a string containing the origin source id from the datapoint from the JSON test data
     * @return a map with the properties needed to generate an expected datapoint to test a google fit datapoint
     */
    public Map<String, Object> createIntegerTestProperties(long intValue, String startDateTime, String endDateTime,
            String sourceOriginId) {

        Map<String, Object> properties = createTestProperties(startDateTime, endDateTime, sourceOriginId);
        properties.put("intValue", intValue);
        return properties;
    }

    /**
     * Creates the properties map used for generating an expected values datapoint to test google fit data points
     * against, used specifically for testing physical activity because it generates a string value as the activity
     * type.
     *
     * @param stringValue an string value from a Google fit JSON test datapoint
     * @param startDateTime a string containing the start timestamp in unix epoch nanoseconds
     * @param endDateTime a string containing the end timestamp in unix epoch nanoseconds
     * @param sourceOriginId a string containing the origin source id from the datapoint from the JSON test data
     * @return a map with the properties needed to generate an expected datapoint to test a google fit datapoint
     */
    public Map<String, Object> createStringTestProperties(String stringValue, String startDateTime, String endDateTime,
            String sourceOriginId) {

        Map<String, Object> properties = createTestProperties(startDateTime, endDateTime, sourceOriginId);
        properties.put("stringValue", stringValue);
        return properties;
    }

    private Map<String, Object> createTestProperties(String startDateTimeString, String endDateTimeString,
            String sourceOriginId) {

        HashMap<String, Object> properties = Maps.newHashMap();
        if (startDateTimeString != null) {
            properties.put("startDateTimeString", startDateTimeString);
        }
        if (endDateTimeString != null) {
            properties.put("endDateTimeString", endDateTimeString);
        }
        if (sourceOriginId != null) {
            properties.put("sourceOriginId", sourceOriginId);
            if (sourceOriginId.endsWith("user_input")) {
                properties.put("modality", DataPointModality.SELF_REPORTED);
            }
        }

        return properties;
    }

    /**
     * Sets the effective time frame for a datapoint builder given a map of properties that contains the key
     * "startDateTimeString" and optionally, "endDateTimeString".
     */
    public void setExpectedEffectiveTimeFrame(T.Builder builder, Map<String, Object> properties) {

        if (properties.containsKey("endDateTimeString")) {
            builder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                    OffsetDateTime.parse((String) properties.get("startDateTimeString")),
                    OffsetDateTime.parse((String) properties.get("endDateTimeString"))));
        }
        else {
            builder.setEffectiveTimeFrame(OffsetDateTime.parse((String) properties.get("startDateTimeString")));
        }
    }
}
