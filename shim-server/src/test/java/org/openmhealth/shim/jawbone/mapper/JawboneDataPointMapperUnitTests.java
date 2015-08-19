package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

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

    JsonNode responseNode;
    public abstract void initializeResponseNode() throws IOException;

    protected static void testDataPointHeader(DataPointHeader testMeasureHeader, Map<String,Object> testProperties){

        assertThat(testMeasureHeader.getBodySchemaId(), equalTo(testProperties.get(HEADER_SCHEMA_ID_KEY)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(
                testProperties.getOrDefault(HEADER_EXTERNAL_ID_KEY, null)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get(
                "source_updated_date_time"),equalTo(OffsetDateTime.parse((String)testProperties.get(
                HEADER_SOURCE_UPDATE_KEY))));
        assertThat(testMeasureHeader.getAdditionalProperties().get(HEADER_SHARED_KEY),equalTo(testProperties.getOrDefault("shared",null)));
    }
}
