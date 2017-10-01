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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.SchemaSupport;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;


/**
 * @author Emerson Farrugia
 */
public abstract class FitbitSleepMeasureDataPointMapperUnitTests<T extends SchemaSupport>
        extends DataPointMapperUnitTests {

    protected JsonNode sleepDateResponseNode;
    protected JsonNode sleepDateEmptySleepListResponseNode;
    protected JsonNode sleepDateRangeResponseNode;
    protected JsonNode sleepDateRangeEmptySleepListResponseNode;

    @BeforeMethod
    public void initializeResponseNode() throws IOException {

        sleepDateResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date.json");
        sleepDateEmptySleepListResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-empty-sleep-list.json");
        sleepDateRangeResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-range.json");
        sleepDateRangeEmptySleepListResponseNode =
                asJsonNode("org/openmhealth/shim/fitbit/mapper/fitbit-sleep-date-range-empty-sleep-list.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(getMapper().asDataPoints(sleepDateResponseNode).size(), equalTo(2));
        assertThat(getMapper().asDataPoints(sleepDateEmptySleepListResponseNode), is(empty()));
        assertThat(getMapper().asDataPoints(sleepDateRangeResponseNode).size(), equalTo(2));
        assertThat(getMapper().asDataPoints(sleepDateRangeEmptySleepListResponseNode), is(empty()));
    }

    protected abstract FitbitSleepMeasureDataPointMapper<T> getMapper();
}
