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
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmhealth.schema.domain.omh.DataPointModality.*;
import static org.openmhealth.shim.ihealth.mapper.IHealthBloodPressureDataPointMapper.BloodPressureUnitType;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBloodPressureDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private IHealthBloodPressureDataPointMapper mapper = new IHealthBloodPressureDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/ihealth/mapper/ihealth-blood-pressure.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    // TODO: Test/handle datapoints that have zero values for BP (awaiting response from iHealth)

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<BloodPressure>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPoints() {

        List<DataPoint<BloodPressure>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BloodPressure expectedBloodPressure = new BloodPressure.Builder(
                new SystolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY, 120),
                new DiastolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY, 90))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T04:04:23-08:00"))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBloodPressure));

        DataPointHeader testHeader = dataPoints.get(0).getHeader();

        testDataPointHeader(testHeader, BloodPressure.SCHEMA_ID, SENSED, "c62b84d9d4b7480a8ff2aef1465aa454",
                OffsetDateTime.parse("2015-09-17T20:04:30Z"));

    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        List<DataPoint<BloodPressure>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BloodPressure expectedBloodPressure = new BloodPressure.Builder(
                new SystolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY, 130),
                new DiastolicBloodPressure(BloodPressureUnit.MM_OF_MERCURY, 95))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-25T08:03:57-06:00"))
                .setUserNotes("BP on the up and up.")
                .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBloodPressure));

        DataPointHeader testHeader = dataPoints.get(1).getHeader();
        
        assertThat(testHeader.getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));

    }

    @Test
    public void asDataPointsShouldReturnCorrectUserNotesWithDataPoints() {

        List<DataPoint<BloodPressure>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        assertThat(dataPoints.get(0).getBody().getUserNotes(), nullValue());
        assertThat(dataPoints.get(1).getBody().getUserNotes(), equalTo("BP on the up and up."));
    }


    @Test
    public void getBloodPressureValueInMmHgReturnsCorrectValueForMmHgUnit() {

        double bpValueFromMmHg = mapper.getBloodPressureValueInMmHg(120, BloodPressureUnitType.mmHg);
        assertThat(bpValueFromMmHg, equalTo(120.0));

        double bpValueFromKpa = mapper.getBloodPressureValueInMmHg(16, BloodPressureUnitType.KPa);
        assertThat(bpValueFromKpa, equalTo(120.009872));

    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void getBloodPressureValueShouldThrowExceptionForInvalidEnum() {

        mapper.getBloodPressureValueInMmHg(12, BloodPressureUnitType.fromIntegerValue(12));
    }


}
