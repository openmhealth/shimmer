/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.ihealth.mapper;

import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.DataPointModality;
import org.openmhealth.schema.domain.omh.SchemaId;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Chris Schaefbauer
 */
public class IHealthDataPointMapperUnitTests extends DataPointMapperUnitTests {

    protected void testDataPointHeader(DataPointHeader testHeader, SchemaId schemaId, DataPointModality modality, String externalId, OffsetDateTime updatedDateTime){

        assertThat(testHeader.getBodySchemaId(), equalTo(schemaId));
        assertThat(testHeader.getAcquisitionProvenance().getModality(), equalTo(modality));
        assertThat(testHeader.getAcquisitionProvenance().getSourceName(),
                equalTo(IHealthDataPointMapper.RESOURCE_API_SOURCE_NAME));
        assertThat(testHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"), equalTo(
                externalId));
        assertThat(testHeader.getAcquisitionProvenance().getAdditionalProperties().get("source_updated_date_time"),
                equalTo(updatedDateTime));
    }

    @Test
    public void setEffectiveTimeFrameShouldUseUtcWhenTimeZoneIsMissing(){

        // Todo: waiting for response from iHealth on missing time zone representation
    }

}
