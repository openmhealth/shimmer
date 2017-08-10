package org.openmhealth.shim.jawbone.mapper;

import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.BodyMassIndex1;
import org.openmhealth.schema.domain.omh.BodyMassIndexUnit1;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.TypedUnitValue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Chris Schaefbauer
 */
public class JawboneBodyMassIndexDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<BodyMassIndex1> {

    private final JawboneBodyMassIndexDataPointMapper mapper = new JawboneBodyMassIndexDataPointMapper();


    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/jawbone/mapper/jawbone-body-events.json");
        initializeEmptyNode();
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWithEmptyResponse() {

        testEmptyNode(mapper);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<BodyMassIndex1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    // TODO why are first and last special?
    @Test
    public void asDataPointsShouldReturnCorrectDataPointWithTimeZone() {

        List<DataPoint<BodyMassIndex1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyMassIndex1 expectedBodyMassIndex = new BodyMassIndex1
                .Builder(new TypedUnitValue<>(BodyMassIndexUnit1.KILOGRAMS_PER_SQUARE_METER, 23))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-10-05T19:52:52-06:00"))
                .build();
        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBodyMassIndex));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put(HEADER_SCHEMA_ID_KEY, BodyMassIndex1.SCHEMA_ID);
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "JM2JlMHcHlUP2mAvWWVlwwNFFVo_4CfQ");
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-10-06T01:52:52Z");
        testProperties.put(HEADER_SHARED_KEY, true);
        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointWithoutTimeZone() {

        List<DataPoint<BodyMassIndex1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        BodyMassIndex1 expectedBodyMassIndex = new BodyMassIndex1
                .Builder(new TypedUnitValue<>(BodyMassIndexUnit1.KILOGRAMS_PER_SQUARE_METER, 22))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-10-06T19:39:01Z"))
                .build();
        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBodyMassIndex));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "JM2JlMHcHlVYbz0vvV-tzteoDrIYcQ7k");
        testProperties.put(HEADER_SCHEMA_ID_KEY, BodyMassIndex1.SCHEMA_ID);
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-10-06T19:39:02Z");
        testProperties.put(HEADER_SHARED_KEY, null);
        testDataPointHeader(dataPoints.get(1).getHeader(), testProperties);
    }
}
