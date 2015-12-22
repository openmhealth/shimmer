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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.openmhealth.schema.domain.omh.BodyWeight.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.ihealth.mapper.IHealthBodyWeightDataPointMapper.IHealthBodyWeightUnit;


/**
 * @author Chris Schaefbauer
 */
public class IHealthBodyWeightDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    private JsonNode responseNode;
    private final IHealthBodyWeightDataPointMapper mapper = new IHealthBodyWeightDataPointMapper();
    private List<DataPoint<BodyWeight>> dataPoints;

    @BeforeClass
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-weight.json");
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

        BodyWeight expectedBodyWeight = new BodyWeight.Builder(
                new MassUnitValue(MassUnit.KILOGRAM, 77.5643875134944))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T12:04:09-08:00"))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBodyWeight));

        DataPointHeader dataPointHeader = dataPoints.get(0).getHeader();

        testDataPointHeader(dataPointHeader, SCHEMA_ID, SENSED, "5fe5893c418b48cd8da7954f8b6c2f36",
                OffsetDateTime.parse("2015-09-17T20:04:17Z"));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSelfReportedDataPoints() {

        BodyWeight expectedBodyWeight =
                new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, 77.56438446044922))
                        .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T14:07:57-06:00"))
                        .setUserNotes("Weight so good, look at me now")
                        .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBodyWeight));

        testDataPointHeader(dataPoints.get(1).getHeader(), SCHEMA_ID, SELF_REPORTED,
                "b702a3a5e998f2fca268df6daaa69871", OffsetDateTime.parse("2015-09-17T20:08:00Z"));

    }

    @Test
    public void asDataPointsShouldReturnCorrectUserNotes() {

        assertThat(dataPoints.get(0).getBody().getUserNotes(), nullValue());
        assertThat(dataPoints.get(1).getBody().getUserNotes(), equalTo("Weight so good, look at me now"));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenWeightValueEqualsZero() throws IOException {

        JsonNode zeroValueNode =
                asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-weight-no-weight-value.json");

        assertThat(mapper.asDataPoints(zeroValueNode), is(empty()));
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWhenWeightListIsEmpty() throws IOException {

        JsonNode emptyListNode = asJsonNode("org/openmhealth/shim/ihealth/mapper/ihealth-weight-empty.json");

        assertThat(mapper.asDataPoints(emptyListNode), is(empty()));
    }

    @Test
    public void getBodyWeightValueForUnitTypeShouldReturnCorrectValueForOmhCompatibleTypes() {

        double bodyWeightValueForUnitType = mapper.getBodyWeightValueForUnitType(66.3,
                IHealthBodyWeightUnit.KG);
        assertThat(bodyWeightValueForUnitType, equalTo(66.3));

        bodyWeightValueForUnitType = mapper.getBodyWeightValueForUnitType(100.5,
                IHealthBodyWeightUnit.LB);

        assertThat(bodyWeightValueForUnitType, equalTo(100.5));
    }

    @Test
    public void getBodyWeightValueForUnitTypeShouldReturnCorrectValueForOmhIncompatibleTypes() {

        double bodyWeightValueForUnitType = mapper.getBodyWeightValueForUnitType(12.4,
                IHealthBodyWeightUnit.STONE);

        assertThat(bodyWeightValueForUnitType, equalTo(78.74372));

    }

    @Test
    public void getOmhUnitInBodyWeightUnitTypeShouldReturnCorrectMassUnits() {

        assertThat(IHealthBodyWeightUnit.KG.getOmhUnit(), equalTo(MassUnit.KILOGRAM));
        assertThat(IHealthBodyWeightUnit.LB.getOmhUnit(), equalTo(MassUnit.POUND));
        assertThat(IHealthBodyWeightUnit.STONE.getOmhUnit(), equalTo(MassUnit.KILOGRAM));
    }


}
