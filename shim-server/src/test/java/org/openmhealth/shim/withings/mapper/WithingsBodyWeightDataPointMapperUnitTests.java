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

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.shim.withings.domain.WithingsBodyMeasureType;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.BodyWeight.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.BODY_WEIGHT;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 */
public class WithingsBodyWeightDataPointMapperUnitTests
        extends WithingsBodyMeasureDataPointMapperUnitTests<BodyWeight> {

    private WithingsBodyWeightDataPointMapper mapper = new WithingsBodyWeightDataPointMapper();
    private JsonNode responseNodeWithGoal;

    @Override
    protected WithingsBodyMeasureDataPointMapper<BodyWeight> getMapper() {
        return mapper;
    }

    @Override
    protected WithingsBodyMeasureType getBodyMeasureType() {
        return BODY_WEIGHT;
    }

    @BeforeTest
    public void initializeAdditionalResponseNodes() throws IOException {

        responseNodeWithGoal = asJsonNode("org/openmhealth/shim/withings/mapper/withings-body-measures-only-goal.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<BodyWeight>> dataPointList = mapper.asDataPoints(responseNode);

        testDataPoint(dataPointList.get(0), 74.126, "2015-05-31T06:06:23Z", "366956482");
        testDataPoint(dataPointList.get(1), 74.128, "2015-04-20T17:13:56Z", "347186704");
    }

    @Test
    public void asDataPointsShouldIgnoreGoalsForBodyMeasures() {

        assertThat(mapper.asDataPoints(responseNodeWithGoal).size(), equalTo(0));
    }

    // TODO: Refactor this out with an "expectedProperties" dictionary for all the inputs and then one for all
    // Withings points
    public void testDataPoint(DataPoint<BodyWeight> testDataPoint, double massValue, String offsetTimeString,
            String externalId) {

        BodyWeight bodyWeightExpected = new BodyWeight.Builder(new MassUnitValue(KILOGRAM, massValue))
                .setEffectiveTimeFrame(OffsetDateTime.parse(offsetTimeString))
                .build();

        assertThat(testDataPoint.getBody(), equalTo(bodyWeightExpected));

        DataPointAcquisitionProvenance testProvenance = testDataPoint.getHeader().getAcquisitionProvenance();

        assertThat(testProvenance.getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(testProvenance.getModality(), equalTo(SENSED));

        assertThat(testDataPoint.getHeader().getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                equalTo(externalId));
        assertThat(testDataPoint.getHeader().getBodySchemaId(), equalTo(SCHEMA_ID));
    }
}
