/*
 * Copyright 2016 Open mHealth
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
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.OxygenSaturation;
import org.openmhealth.schema.domain.omh.TypedUnitValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.OxygenSaturation.*;
import static org.openmhealth.schema.domain.omh.OxygenSaturation.MeasurementMethod.PULSE_OXIMETRY;
import static org.openmhealth.schema.domain.omh.OxygenSaturation.MeasurementSystem.*;
import static org.openmhealth.schema.domain.omh.PercentUnit.PERCENT;
import static org.openmhealth.shim.ihealth.mapper.IHealthDataPointMapper.*;


/**
 * @author Chris Schaefbauer
 */
public class IHealthOxygenSaturationDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private final IHealthOxygenSaturationDataPointMapper mapper = new IHealthOxygenSaturationDataPointMapper();
    private List<DataPoint<OxygenSaturation>> dataPoints;

    @BeforeClass
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-spo2.json");
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

        OxygenSaturation expectedOxygenSaturation = new OxygenSaturation.Builder(new TypedUnitValue<>(PERCENT, 99))
                .setMeasurementMethod(PULSE_OXIMETRY)
                .setMeasurementSystem(PERIPHERAL_CAPILLARY)
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-23T15:46:00-06:00"))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedOxygenSaturation));
        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getModality(), equalTo(SENSED));
        assertThat(dataPoints.get(0).getHeader().getBodySchemaId(), equalTo(SCHEMA_ID));
        assertThat(dataPoints.get(0).getHeader().getAcquisitionProvenance().getSourceName(),
                equalTo(RESOURCE_API_SOURCE_NAME));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedAcquisitionProvenance() {

        assertThat(dataPoints.get(1).getHeader().getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
    }

    @Test
    public void asDataPointsShouldReturnCorrectUserNotesWithDataPoints() {

        assertThat(dataPoints.get(0).getBody().getUserNotes(), nullValue());
        assertThat(dataPoints.get(1).getBody().getUserNotes(), equalTo("Satch on satch "));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenOxygenSaturationListIsEmpty() {

        assertThat(mapper.asDataPoints(asJsonNode("/org/openmhealth/shim/ihealth/mapper/ihealth-spo2-empty.json")),
                is(empty()));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointWhenOxygenSaturationDataIsNotPresent() throws IOException {

        assertThat(mapper.asDataPoints(
                asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-spo2-no-oxygen-saturation.json")), is(empty()));
    }
}
