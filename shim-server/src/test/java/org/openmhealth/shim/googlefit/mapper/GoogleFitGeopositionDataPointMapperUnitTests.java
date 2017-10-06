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

package org.openmhealth.shim.googlefit.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.Geoposition;
import org.openmhealth.schema.domain.omh.LengthUnit;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.Geoposition.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.PlaneAngleUnit.DEGREE_OF_ARC;


/**
 * @author Emerson Farrugia
 */
public class GoogleFitGeopositionDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<Geoposition> {

    private final GoogleFitGeopositionDataPointMapper mapper = new GoogleFitGeopositionDataPointMapper();

    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-location-samples.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsWithoutElevation() {

        List<DataPoint<Geoposition>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(
                dataPoints.get(1),
                createTestProperties(
                        "2017-10-04T13:45:54Z",
                        null,
                        "raw:com.google.location.sample:com.google.android.gms:LGE:Nexus 5:bar:live_location",
                        SCHEMA_ID,
                        51.233600616455078, -0.57613241672515869, 43.0, 50.0));
    }

    @Override
    public void assertThatMeasureMatches(Geoposition testMeasure, GoogleFitTestProperties testProperties) {

        Geoposition expectedGeoposition =
                new Geoposition.Builder(
                        DEGREE_OF_ARC.newUnitValue(testProperties.getFpValue(0)),
                        DEGREE_OF_ARC.newUnitValue(testProperties.getFpValue(1)),
                        testProperties.getEffectiveTimeFrame().orElseThrow(IllegalArgumentException::new))
                        .setElevation(LengthUnit.METER.newUnitValue(testProperties.getFpValue(3)))
                        .build();

        assertThat(testMeasure, equalTo(expectedGeoposition));
    }
}
