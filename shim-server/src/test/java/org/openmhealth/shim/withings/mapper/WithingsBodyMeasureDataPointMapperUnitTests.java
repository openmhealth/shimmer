package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


/**
 * Created by Chris Schaefbauer on 6/29/15.
 */
public class WithingsBodyMeasureDataPointMapperUnitTests extends DataPointMapperUnitTests {

    WithingsBodyWeightDataPointMapper mapper = new WithingsBodyWeightDataPointMapper();
    JsonNode responseNode;

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/withings/mapper/withings-body-measures.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints(){
        List<DataPoint<BodyWeight>> dataPointList = mapper.asDataPoints(Collections.singletonList(responseNode));
        assertThat(dataPointList.size(),equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints(){
        List<DataPoint<BodyWeight>> dataPointList = mapper.asDataPoints(Collections.singletonList(responseNode));

        BodyWeight.Builder bodyWeightExpectedMeasureBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, 74.126));
        bodyWeightExpectedMeasureBuilder.setEffectiveTimeFrame(OffsetDateTime.parse("2015-05-30T23:06:23-07:00"));
        BodyWeight bodyWeightExpected = bodyWeightExpectedMeasureBuilder.build();

        BodyWeight bodyWeightTestMeasure = dataPointList.get(0).getBody();
        assertThat(bodyWeightTestMeasure,equalTo(bodyWeightExpected));

        bodyWeightExpectedMeasureBuilder = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, 74.128));
        bodyWeightExpectedMeasureBuilder.setEffectiveTimeFrame(OffsetDateTime.parse("2015-04-20T10:13:56-07:00"));
        bodyWeightExpected = bodyWeightExpectedMeasureBuilder.build();

        bodyWeightTestMeasure = dataPointList.get(1).getBody();
        assertThat(bodyWeightTestMeasure,equalTo(bodyWeightExpected));

    }
}
