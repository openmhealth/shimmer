package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.schema.domain.omh.StepCount;
import org.openmhealth.schema.domain.omh.TimeInterval;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.*;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shim.jawbone.mapper.JawboneDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public abstract class JawboneDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    static final String HEADER_EXTERNAL_ID_KEY = "externalId";
    static final String HEADER_SCHEMA_ID_KEY = "schemaId";
    static final String HEADER_SOURCE_UPDATE_KEY = "sourceUpdatedDateTime";
    static final String HEADER_SHARED_KEY = "shared";
    static final String HEADER_SENSED_KEY = "sensed";

    JawboneDataPointMapper<T> mapper = new JawboneDataPointMapper<T>() {
        @Override
        protected Optional<T> getMeasure(JsonNode listEntryNode) {
            return null;
        }
    };

    JsonNode responseNode;

    public abstract void initializeResponseNode() throws IOException;

    /* Tests */

    @Test
    public void setEffectiveTimeFrameShouldSetCorrectForDateTime() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"tz\": \"GMT-0600\"\n" +
                "},\n" +
                "\"time_created\": 1438747200,\n" +
                "\"time_updated\": 1439867504\n" +
                "}");

        StepCount.Builder testBuilder = new StepCount.Builder(10);
        mapper.setEffectiveTimeFrame(testBuilder, testDateTimeNode);
        StepCount stepCount = testBuilder.build();

        assertThat(stepCount.getEffectiveTimeFrame().getDateTime(),
                equalTo(OffsetDateTime.parse("2015-08-04T22:00:00-06:00")));
    }

    @Test
    public void setEffectiveTimeFrameShouldSetCorrectTimeInterval() throws IOException {

        JsonNode testDateTimeNode = objectMapper.readTree("{\n" +
                "\"details\": {\n" +
                "\"tz\": \"GMT-0200\"\n" +
                "},\n" +
                "\"time_created\": 1439990403,\n" +
                "\"time_updated\": 1439867504,\n" +
                "\"time_completed\": 1439994003\n" +
                "}");

        StepCount.Builder testBuilder = new StepCount.Builder(10);
        mapper.setEffectiveTimeFrame(testBuilder, testDateTimeNode);
        StepCount stepCount = testBuilder.build();

        assertThat(stepCount.getEffectiveTimeFrame().getTimeInterval(), equalTo(
                TimeInterval.ofStartDateTimeAndEndDateTime(OffsetDateTime.parse("2015-08-19T11:20:03-02:00"),
                        OffsetDateTime.parse("2015-08-19T12:20:03-02:00"))));
    }

    /* Test helper classes */

    protected static void testDataPointHeader(DataPointHeader testMeasureHeader, Map<String, Object> testProperties) {

        assertThat(testMeasureHeader.getBodySchemaId(), equalTo(testProperties.get(HEADER_SCHEMA_ID_KEY)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"), equalTo(
                testProperties.getOrDefault(HEADER_EXTERNAL_ID_KEY, null)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get(
                "source_updated_date_time"), equalTo(OffsetDateTime.parse((String) testProperties.get(
                HEADER_SOURCE_UPDATE_KEY))));
        assertThat(testMeasureHeader.getAdditionalProperties().get(HEADER_SHARED_KEY),
                equalTo(testProperties.getOrDefault(HEADER_SHARED_KEY, null)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getModality(),
                equalTo(testProperties.getOrDefault(HEADER_SENSED_KEY, null)));
    }
}
