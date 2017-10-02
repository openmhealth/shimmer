/*
 * Copyright 2017 Open mHealth
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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.openmhealth.shim.common.mapper.JsonNodeMappingException;
import org.openmhealth.shim.withings.domain.WithingsBodyMeasureType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class WithingsBodyMeasureDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected JsonNode responseNode;

    protected abstract WithingsBodyMeasureDataPointMapper<T> getMapper();

    protected abstract WithingsBodyMeasureType getBodyMeasureType();

    @BeforeMethod
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/withings/mapper/withings-body-measures.json");
    }

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void getValueForMeasureTypeShouldThrowExceptionOnDuplicateMeasureTypes() throws Exception {

        JsonNode measuresNode = objectMapper.readTree("[\n" +
                "    {\n" +
                "        \"type\": " + getBodyMeasureType().getMagicNumber() + ",\n" +
                "        \"unit\": 0,\n" +
                "        \"value\": 68\n" +
                "    },\n" +
                "    {\n" +
                "        \"type\": " + getBodyMeasureType().getMagicNumber() + ",\n" +
                "        \"unit\": 0,\n" +
                "        \"value\": 104\n" +
                "    }\n" +
                "]");

        getMapper().getValueForMeasureType(measuresNode, getBodyMeasureType());
    }
}
