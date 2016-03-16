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

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.BloodPressure.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.BloodPressureUnit.MM_OF_MERCURY;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBloodPressureDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private IHealthBloodPressureDataPointMapper mapper = new IHealthBloodPressureDataPointMapper();
    private List<DataPoint<BloodPressure>> dataPoints;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-bp.json");
    }

    @BeforeMethod
    public void initializeDataPoints() {

        dataPoints = mapper.asDataPoints(responseNode);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        BloodPressure expectedBloodPressure = new BloodPressure.Builder(
                new SystolicBloodPressure(MM_OF_MERCURY, 120),
                new DiastolicBloodPressure(MM_OF_MERCURY, 90))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T12:04:23-08:00"))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBloodPressure));

        DataPointHeader testHeader = dataPoints.get(0).getHeader();

        testDataPointHeader(testHeader, SCHEMA_ID, SENSED, "c62b84d9d4b7480a8ff2aef1465aa454",
                OffsetDateTime.parse("2015-09-17T20:04:30Z"));

    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        BloodPressure expectedBloodPressure = new BloodPressure.Builder(
                new SystolicBloodPressure(MM_OF_MERCURY, 130),
                new DiastolicBloodPressure(MM_OF_MERCURY, 95))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T14:07:45-06:00"))
                .setUserNotes("BP on the up and up.")
                .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBloodPressure));

        DataPointHeader testHeader = dataPoints.get(1).getHeader();

        assertThat(testHeader.getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));

    }

    @Test
    public void asDataPointsShouldReturnCorrectUserNotesWithDataPoints() {

        assertThat(dataPoints.get(0).getBody().getUserNotes(), nullValue());
        assertThat(dataPoints.get(1).getBody().getUserNotes(), equalTo("BP on the up and up."));
    }

    @Test
    public void asDataPointsShouldReturnEmptyListWhenEmptyIHealthResponse() {

        JsonNode emptyNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-bp-empty.json");

        assertThat(mapper.asDataPoints(emptyNode), is(empty()));
    }

    @Test
    public void getBloodPressureValueInMmHgReturnsCorrectValueForMmHgUnit() {

        double bpValueFromMmHg = mapper.getBloodPressureValueInMmHg(120, 0);
        assertThat(bpValueFromMmHg, equalTo(120.0));

        double bpValueFromKpa = mapper.getBloodPressureValueInMmHg(16, 1);
        assertThat(bpValueFromKpa, equalTo(120.0));

    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void getBloodPressureValueShouldThrowExceptionForInvalidEnum() {

        mapper.getBloodPressureValueInMmHg(12, 12);
    }


}
