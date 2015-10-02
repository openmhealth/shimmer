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
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Chris Schaefbauer
 */
public class IHealthHeartRateDataPointMapperUnitTests extends IHealthDataPointMapperUnitTests {

    JsonNode bpNode;
    JsonNode spo2Node;
    private IHealthHeartRateDataPointMapper mapper = new IHealthHeartRateDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource = new ClassPathResource("/org/openmhealth/shim/ihealth/mapper/ihealth-heart-rate-from-bp.json");
        bpNode = objectMapper.readTree(resource.getInputStream());

        resource = new ClassPathResource("/org/openmhealth/shim/ihealth/mapper/ihealth-heart-rate-from-spo2.json");
        spo2Node = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(Lists.newArrayList(bpNode, spo2Node));
        assertThat(dataPoints.size(),equalTo(4));
    }

    @Test
    public void asDataPointsShouldReturnCorrectSensedDataPointsFromBpResponse(){

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(Lists.newArrayList(bpNode, spo2Node));

        HeartRate.Builder expectedHeartRateBuilder = new HeartRate.Builder(100)
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-09-17T04:04:23-08:00"));
        HeartRate expectedSensedHeartRate = expectedHeartRateBuilder.build();
        assertThat(dataPoints.get(0).getBody(),equalTo(expectedSensedHeartRate));

    }

}
