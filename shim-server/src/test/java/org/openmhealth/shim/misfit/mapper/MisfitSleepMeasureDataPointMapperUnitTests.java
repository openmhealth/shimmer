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

package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.SchemaSupport;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.openmhealth.shim.common.mapper.JsonNodeMappingException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Emerson Farrugia
 */
public abstract class MisfitSleepMeasureDataPointMapperUnitTests<T extends SchemaSupport>
        extends DataPointMapperUnitTests {

    protected JsonNode sleepsResponseNode;

    @BeforeMethod
    public void initializeResponseNode() throws IOException {

        sleepsResponseNode
                = asJsonNode("org/openmhealth/shim/misfit/mapper/misfit-sleeps.json");
    }

    protected abstract MisfitSleepMeasureDataPointMapper<T> getMapper();

    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void asDataPointsShouldThrowExceptionOnEmptySleepDetails() throws IOException {

        JsonNode node = objectMapper.readTree("{\n" +
                "    \"sleeps\": [\n" +
                "        {\n" +
                "            \"id\": \"54fa13a8440f705a7406845f\",\n" +
                "            \"autoDetected\": false,\n" +
                "            \"startTime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "            \"duration\": 2580,\n" +
                "            \"sleepDetails\": []\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        getMapper().asDataPoints(node);
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfOnlyAwake() throws IOException {

        JsonNode node = objectMapper.readTree("{\n" +
                "    \"sleeps\": [\n" +
                "        {\n" +
                "            \"id\": \"54fa13a8440f705a7406845f\",\n" +
                "            \"autoDetected\": false,\n" +
                "            \"startTime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "            \"duration\": 2580,\n" +
                "            \"sleepDetails\": [\n" +
                "                {\n" +
                "                    \"datetime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "                    \"value\": 1\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        List<DataPoint<T>> dataPoints = getMapper().asDataPoints(node);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints, empty());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<T>> dataPoints = getMapper().asDataPoints(sleepsResponseNode);

        assertThat(dataPoints, notNullValue());
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListIfEmptyResponse() throws IOException {

        JsonNode emptyNode = objectMapper.readTree("{\n" +
                "    \"sleeps\": []\n" +
                "}");

        assertThat(getMapper().asDataPoints(emptyNode), empty());
    }

    @Test
    public void asDataPointsShouldSetModalityToSensedWhenAutoDetectedIsTrue() throws IOException {

        JsonNode node = objectMapper.readTree("{\n" +
                "    \"sleeps\": [\n" +
                "        {\n" +
                "            \"id\": \"54fa13a8440f705a7406845f\",\n" +
                "            \"autoDetected\": true,\n" +
                "            \"startTime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "            \"duration\": 2580,\n" +
                "            \"sleepDetails\": [\n" +
                "                {\n" +
                "                    \"datetime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "                    \"value\": 2\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        List<DataPoint<T>> dataPoints = getMapper().asDataPoints(node);

        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getModality(), equalTo(SENSED));
    }

    @Test
    public void asDataPointsShouldNotSetModalityWhenAutoDetectedIsFalse() throws IOException {

        JsonNode node = objectMapper.readTree("{\n" +
                "    \"sleeps\": [\n" +
                "        {\n" +
                "            \"id\": \"54fa13a8440f705a7406845f\",\n" +
                "            \"autoDetected\": false,\n" +
                "            \"startTime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "            \"duration\": 2580,\n" +
                "            \"sleepDetails\": [\n" +
                "                {\n" +
                "                    \"datetime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "                    \"value\": 2\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        List<DataPoint<T>> dataPoints = getMapper().asDataPoints(node);

        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getModality(), nullValue());
    }

    @Test
    public void asDataPointsShouldNotSetModalityWhenAutoDetectedIsMissing() throws IOException {

        JsonNode node = objectMapper.readTree("{\n" +
                "    \"sleeps\": [\n" +
                "        {\n" +
                "            \"id\": \"54fa13a8440f705a7406845f\",\n" +
                "            \"startTime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "            \"duration\": 2580,\n" +
                "            \"sleepDetails\": [\n" +
                "                {\n" +
                "                    \"datetime\": \"2015-02-24T21:40:59-05:00\",\n" +
                "                    \"value\": 2\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        List<DataPoint<T>> dataPoints = getMapper().asDataPoints(node);

        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getModality(), nullValue());
    }
}