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

import org.openmhealth.schema.domain.omh.BodyHeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.shim.withings.domain.WithingsBodyMeasureType;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.BodyHeight.SCHEMA_ID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.BODY_HEIGHT;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class WithingsBodyHeightDataPointMapperUnitTests
        extends WithingsBodyMeasureDataPointMapperUnitTests<BodyHeight> {

    private WithingsBodyHeightDataPointMapper mapper = new WithingsBodyHeightDataPointMapper();

    @Override
    protected WithingsBodyMeasureDataPointMapper<BodyHeight> getMapper() {
        return mapper;
    }

    @Override
    protected WithingsBodyMeasureType getBodyMeasureType() {
        return BODY_HEIGHT;
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<BodyHeight>> actualDataPoints = mapper.asDataPoints(responseNode);

        BodyHeight expectedBodyHeight = new BodyHeight.Builder(new LengthUnitValue(METER, 1.93))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-02-23T19:24:49Z"))
                .build();

        assertThat(actualDataPoints.get(0).getBody(), equalTo(expectedBodyHeight));

        DataPointHeader actualDataPointHeader = actualDataPoints.get(0).getHeader();
        assertThat(actualDataPointHeader.getBodySchemaId(), equalTo(SCHEMA_ID));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                equalTo("320419189"));
    }
}
