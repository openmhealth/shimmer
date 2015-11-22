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
import org.openmhealth.schema.domain.omh.HeartRate;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;


/**
 * @author Chris Schaefbauer
 */
public class IHealthDatapointMapperDateTimeUnitTests extends IHealthDataPointMapperUnitTests {

    @Test
    public void setEffectiveTimeFrameShouldNotAddTimeFrameWhenTimeZoneIsMissing() throws IOException {

        HeartRate.Builder builder = new HeartRate.Builder(45);

        JsonNode timeInfoNode = createResponseNodeWithTimeZone(null);
        IHealthDataPointMapper.setEffectiveTimeFrameIfExists(timeInfoNode, builder);

        assertThat(builder.build().getEffectiveTimeFrame(), nullValue());
    }

    @Test
    public void setEffectiveTimeFrameReturnsTimeFrameInUtcWhenTimeZoneEqualsZero() throws IOException {

        HeartRate.Builder builder = new HeartRate.Builder(45);

        JsonNode timeInfoNode = createResponseNodeWithTimeZone("0");

        IHealthDataPointMapper.setEffectiveTimeFrameIfExists(timeInfoNode, builder);

        assertThat(builder.build().getEffectiveTimeFrame(), notNullValue());
        assertThat(builder.build().getEffectiveTimeFrame().getDateTime(), equalTo(
                OffsetDateTime.parse("2015-11-17T18:24:23Z")));
    }

    @Test
    public void setEffectiveTimeFrameShouldNotAddTimeFrameWhenTimeZoneIsEmpty() throws IOException{

        HeartRate.Builder builder = new HeartRate.Builder(45);

        JsonNode timeInfoNode = createResponseNodeWithTimeZone("\"\"");

        IHealthDataPointMapper.setEffectiveTimeFrameIfExists(timeInfoNode, builder);

        assertThat(builder.build().getEffectiveTimeFrame(), nullValue());
    }

    public JsonNode createResponseNodeWithTimeZone(String timezoneString) throws IOException {

        if(timezoneString == null){
            return objectMapper.readTree("{\"MDate\": 1447784663,\n" +
                    "            \"Steps\": 100}\n");
        }
        else{
            return objectMapper.readTree("{\"MDate\": 1447784663,\n" +
                    "            \"Steps\": 100,\n" +
                    "\"TimeZone\": "+timezoneString+"}\n");
        }
    }
}
