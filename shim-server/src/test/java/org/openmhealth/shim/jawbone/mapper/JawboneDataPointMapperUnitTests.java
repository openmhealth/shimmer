package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shim.jawbone.mapper.JawboneDataPointMapper.RESOURCE_API_SOURCE_NAME;


// TODO clean up
/**
 * @author Chris Schaefbauer
 */
public abstract class JawboneDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    static final String HEADER_EXTERNAL_ID_KEY = "externalId";
    static final String HEADER_SCHEMA_ID_KEY = "schemaId";
    static final String HEADER_SOURCE_UPDATE_KEY = "sourceUpdatedDateTime";
    static final String HEADER_SHARED_KEY = "shared";
    static final String HEADER_SENSED_KEY = "sensed";

    JawboneDataPointMapper<T> sensedMapper = new JawboneDataPointMapper<T>() {
        @Override
        protected Optional<T> getMeasure(JsonNode listEntryNode) {
            return null;
        }

        @Override
        protected boolean isSensed(JsonNode listEntryNode) {
            return true;
        }
    };

    JawboneDataPointMapper<T> unsensedMapper = new JawboneDataPointMapper<T>() {
        @Override
        protected Optional<T> getMeasure(JsonNode listEntryNode) {
            return null;
        }

        @Override
        protected boolean isSensed(JsonNode listEntryNode) {
            return false;
        }
    };

    JsonNode responseNode;
    JsonNode emptyNode;

    public abstract void initializeResponseNodes() throws IOException;

    public void initializeEmptyNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-empty.json");
        emptyNode = objectMapper.readTree(resource.getInputStream());
    }

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

        StepCount1.Builder testBuilder = new StepCount1.Builder(10);
        sensedMapper.setEffectiveTimeFrame(testBuilder, testDateTimeNode);
        StepCount1 stepCount = testBuilder.build();

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

        StepCount1.Builder testBuilder = new StepCount1.Builder(10);
        sensedMapper.setEffectiveTimeFrame(testBuilder, testDateTimeNode);
        StepCount1 stepCount = testBuilder.build();

        assertThat(stepCount.getEffectiveTimeFrame().getTimeInterval(), equalTo(
                TimeInterval.ofStartDateTimeAndEndDateTime(OffsetDateTime.parse("2015-08-19T11:20:03-02:00"),
                        OffsetDateTime.parse("2015-08-19T12:20:03-02:00"))));
    }

    /* Header tests */
    @Test
    public void getHeaderShouldReturnSensedInHeaderWhenIsSensedIsTrue() throws IOException {

        JsonNode headerInfoNode = objectMapper.readTree("{}");
        DataPointHeader header = sensedMapper.getHeader(headerInfoNode, (T) new StepCount1.Builder(10).build());
        assertThat(header.getAcquisitionProvenance().getModality(), equalTo(DataPointModality.SENSED));
    }

    @Test
    public void getHeaderShouldReturnNullForSensedInHeaderWhenIsSensedIsFalse() throws IOException {

        JsonNode headerInfoNode = objectMapper.readTree("{}");
        DataPointHeader header = unsensedMapper.getHeader(headerInfoNode, (T) new StepCount1.Builder(10).build());
        assertThat(header.getAcquisitionProvenance().getModality(), nullValue());
    }

    @Test
    public void getHeaderShouldReturnTrueForSharedInHeaderWhenNodeIsShared() throws IOException {

        JsonNode sharedNode = objectMapper.readTree("{\n" +
                "\"shared\": true\n" +
                "}");
        DataPointHeader sharedHeader = sensedMapper.getHeader(sharedNode, (T) new StepCount1.Builder(10).build());
        assertThat(sharedHeader.getAdditionalProperty("shared").get(), equalTo(true));
    }

    @Test
    public void getHeaderShouldReturnFalseForSharedInHeaderWhenNodeIsNotShared() throws IOException {

        JsonNode sharedNode = objectMapper.readTree("{\n" +
                "\"shared\": false\n" +
                "}");
        DataPointHeader sharedHeader = sensedMapper.getHeader(sharedNode, (T) new StepCount1.Builder(10).build());
        assertThat(sharedHeader.getAdditionalProperty("shared").get(), equalTo(false));
    }

    @Test
    public void getHeaderShouldReturnNullForSharedInHeaderWhenSharedPropertyDoesNotExist() throws IOException {

        JsonNode sharedNode = objectMapper.readTree("{}");
        DataPointHeader sharedHeader = sensedMapper.getHeader(sharedNode, (T) new StepCount1.Builder(10).build());
        assertThat(sharedHeader.getAdditionalProperties().get("shared"), nullValue());
    }

    @Test
    public void getHeaderShouldReturnExternalIdInHeaderWhenXidPropertyExists() throws IOException {

        JsonNode xidNode = objectMapper.readTree("{\n" +
                "\"xid\": \"40F7_htRRnT8Vo7nRBZO1X\"\n" +
                "}");
        DataPointHeader headerWithXid = sensedMapper.getHeader(xidNode, (T) new StepCount1.Builder(10).build());
        assertThat(headerWithXid.getAcquisitionProvenance().getAdditionalProperty("external_id").get(), equalTo(
                "40F7_htRRnT8Vo7nRBZO1X"));
    }

    @Test
    public void getHeaderShouldReturnNullForExternalIdInHeaderWhenXidPropertyDoesNotExist() throws IOException {

        JsonNode xidNode = objectMapper.readTree("{}");
        DataPointHeader headerWithXid = sensedMapper.getHeader(xidNode, (T) new StepCount1.Builder(10).build());
        assertThat(headerWithXid.getAcquisitionProvenance().getAdditionalProperties().get("external_id"), nullValue());
    }

    @Test
    public void getHeaderShouldReturnCorrectUpdatedTimestampWhenUpdatedPropertyExists() throws IOException {

        JsonNode updatedNode = objectMapper.readTree("{\n" +
                "\"time_updated\": 1439354240\n" +
                "}");
        DataPointHeader headerWithTimeUpdated =
                sensedMapper.getHeader(updatedNode, (T) new StepCount1.Builder(10).build());
        assertThat(headerWithTimeUpdated.getAcquisitionProvenance().getAdditionalProperty("source_updated_date_time")
                .get(), equalTo(OffsetDateTime.parse("2015-08-12T04:37:20Z")));
    }

    @Test
    public void getHeaderShouldReturnNullWhenUpdatedPropertyDoesNotExist() throws IOException {

        JsonNode updatedNode = objectMapper.readTree("{}");
        DataPointHeader headerWithTimeUpdated =
                sensedMapper.getHeader(updatedNode, (T) new StepCount1.Builder(10).build());
        assertThat(headerWithTimeUpdated.getAcquisitionProvenance().getAdditionalProperties()
                .get("source_updated_date_time"), nullValue());
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

    protected void testEmptyNode(JawboneDataPointMapper mapper) {

        List<DataPoint<?>> dataPoints = mapper.asDataPoints(singletonList(emptyNode));
        assertThat(dataPoints.size(), equalTo(0));
    }
}
