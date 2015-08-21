package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    @Test
    public void parseZoneShouldReturnCorrectOlsonTimeZoneId() throws IOException {

        JsonNode testOlsonTimeZoneJsonNode = objectMapper.readTree("\"America/New_York\"");

        ZoneId testZoneId = JawboneDataPointMapper.parseZone(testOlsonTimeZoneJsonNode);
        ZoneId expectedZoneId = ZoneId.of("America/New_York");
        assertThat(testZoneId,equalTo(expectedZoneId));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200),testZoneId);
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2015-08-05T00:00:00-04:00");
        assertThat(testOffsetDateTime,equalTo(expectedOffsetDateTime));



    }

    @Test
    public void parseZoneShouldReturnCorrectSecondsOffsetTimeZoneId() throws IOException {

        JsonNode testSecondOffsetTimeZoneJsonNode = objectMapper.readTree("-21600");

        ZoneId testZoneId =  JawboneDataPointMapper.parseZone(testSecondOffsetTimeZoneJsonNode).normalized();
        ZoneId expectedZoneId = ZoneId.of("-06:00");
        assertThat(testZoneId.getRules(),equalTo(expectedZoneId.getRules()));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200), testZoneId);
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2015-08-04T22:00:00-06:00");
        assertThat(testOffsetDateTime,equalTo(expectedOffsetDateTime));

    }

    @Test
    public void parseZoneShouldReturnCorrectGmtOffsetTimeZoneID() throws IOException {

        JsonNode testGmtOffsetTimeZoneJsonNode = objectMapper.readTree("\"GMT-0600\"");

        ZoneId testZoneId = JawboneDataPointMapper.parseZone(testGmtOffsetTimeZoneJsonNode);
        ZoneId expectedZoneId = ZoneId.of("-06:00");
        assertThat(testZoneId.getRules(),equalTo(expectedZoneId.getRules()));

        OffsetDateTime testOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1438747200), testZoneId);
        OffsetDateTime expectedOffsetDateTime = OffsetDateTime.parse("2015-08-04T22:00:00-06:00");
        assertThat(testOffsetDateTime,equalTo(expectedOffsetDateTime));
    }

    protected static void testDataPointHeader(DataPointHeader testMeasureHeader, Map<String,Object> testProperties){

        assertThat(testMeasureHeader.getBodySchemaId(), equalTo(testProperties.get(HEADER_SCHEMA_ID_KEY)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(
                testProperties.getOrDefault(HEADER_EXTERNAL_ID_KEY, null)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get(
                "source_updated_date_time"),equalTo(OffsetDateTime.parse((String)testProperties.get(
                HEADER_SOURCE_UPDATE_KEY))));
        assertThat(testMeasureHeader.getAdditionalProperties().get(HEADER_SHARED_KEY),equalTo(testProperties.getOrDefault(HEADER_SHARED_KEY,null)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getModality(),equalTo(testProperties.getOrDefault(HEADER_SENSED_KEY,null)));
    }
}
