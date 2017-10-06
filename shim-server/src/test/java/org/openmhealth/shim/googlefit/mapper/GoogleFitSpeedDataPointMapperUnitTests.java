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
import org.openmhealth.schema.domain.omh.Speed;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.Speed.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.SpeedUnit.METERS_PER_SECOND;


/**
 * @author Emerson Farrugia
 */
public class GoogleFitSpeedDataPointMapperUnitTests extends GoogleFitDataPointMapperUnitTests<Speed> {

    private final GoogleFitSpeedDataPointMapper mapper = new GoogleFitSpeedDataPointMapper();

    @BeforeClass
    @Override
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/googlefit/mapper/googlefit-merge-speed.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<Speed>> dataPoints = mapper.asDataPoints(responseNode);

        assertThatDataPointMatches(dataPoints.get(0),
                createFloatingPointTestProperties(0.75, "2017-10-04T13:55:56Z", null,
                        "derived:com.google.speed:com.google.android.gms:LGE:Nexus 5:foo:live_gps_speed", SCHEMA_ID));
    }

    @Override
    public void assertThatMeasureMatches(Speed testMeasure, GoogleFitTestProperties testProperties) {

        Speed expectedSpeed =
                new Speed.Builder(
                        METERS_PER_SECOND.newUnitValue(testProperties.getFpValue()),
                        testProperties.getEffectiveTimeFrame().orElseThrow(IllegalArgumentException::new))
                        .build();

        assertThat(testMeasure, equalTo(expectedSpeed));
    }
}
