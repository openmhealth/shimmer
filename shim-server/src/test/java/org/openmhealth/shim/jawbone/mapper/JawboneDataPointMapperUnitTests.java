package org.openmhealth.shim.jawbone.mapper;

import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.shim.jawbone.mapper.JawboneDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public class JawboneDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    protected void testDataPointHeader(DataPointHeader testMeasureHeader, Map<String,Object> testProperties){

        assertThat(testMeasureHeader.getBodySchemaId(),equalTo(testProperties.get("schemaId")));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getSourceName(),equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),equalTo(
                testProperties.getOrDefault("externalId",null)));
        assertThat(testMeasureHeader.getAcquisitionProvenance().getAdditionalProperties().get(
                "source_updated_date_time"),equalTo(OffsetDateTime.parse((String)testProperties.get("sourceUpdatedDateTime"))));
        assertThat(testMeasureHeader.getAdditionalProperties().get("shared"),equalTo(testProperties.getOrDefault("shared",null)));
    }
}
