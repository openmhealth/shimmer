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

import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.withings.domain.WithingsBodyMeasureType;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.BloodPressure.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.BloodPressureUnit.MM_OF_MERCURY;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.DIASTOLIC_BLOOD_PRESSURE;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class WithingsBloodPressureDataPointMapperUnitTests
        extends WithingsBodyMeasureDataPointMapperUnitTests<BloodPressure> {

    private WithingsBloodPressureDataPointMapper mapper = new WithingsBloodPressureDataPointMapper();

    @Override
    protected WithingsBodyMeasureDataPointMapper<BloodPressure> getMapper() {
        return mapper;
    }

    @Override
    protected WithingsBodyMeasureType getBodyMeasureType() {
        return DIASTOLIC_BLOOD_PRESSURE;
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<BloodPressure>> actualDataPoints = mapper.asDataPoints(singletonList(responseNode));

        BloodPressure expectedBloodPressure = new BloodPressure.Builder(
                new SystolicBloodPressure(MM_OF_MERCURY, 104.0),
                new DiastolicBloodPressure(MM_OF_MERCURY, 68.0))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-05-31T06:06:23Z"))
                .build();

        assertThat(actualDataPoints.get(0).getBody(), equalTo(expectedBloodPressure));

        DataPointHeader actualDataPointHeader = actualDataPoints.get(0).getHeader();
        assertThat(actualDataPointHeader.getBodySchemaId(), equalTo(SCHEMA_ID));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getModality(), equalTo(SENSED));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                equalTo("366956482"));
    }
}
