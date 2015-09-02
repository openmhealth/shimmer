package org.openmhealth.shim.jawbone.mapper;

import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.*;
import org.springframework.core.io.ClassPathResource;
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
public class JawboneBodyMassIndexDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<BodyMassIndex> {


    JawboneBodyMassIndexDataPointMapper mapper = new JawboneBodyMassIndexDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-body-events.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
        initializeEmptyNode();
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWithEmptyResponse() {

        testEmptyNode(mapper);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectFirstDataPoint() {

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyMassIndex expectedBodyMassIndex = new BodyMassIndex
                .Builder(new TypedUnitValue<>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER, 24))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-08-11T22:37:18-06:00"))
                .build();
        assertThat(dataPoints.get(0).getBody(), equalTo(expectedBodyMassIndex));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put(HEADER_SCHEMA_ID_KEY, BodyMassIndex.SCHEMA_ID);
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "QkfTizSpRdukQY3ns4PYbkucZTM5yPMg");
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-08-13T08:23:58Z");
        testProperties.put(HEADER_SHARED_KEY, true);
        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);
    }

    @Test
    public void asDataPointsShouldReturnCorrectLastDataPoint() {

        List<DataPoint<BodyMassIndex>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        BodyMassIndex expectedBodyMassIndex = new BodyMassIndex
                .Builder(new TypedUnitValue<>(BodyMassIndexUnit.KILOGRAMS_PER_SQUARE_METER, 25.2))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-08-06T23:36:57-06:00"))
                .build();
        assertThat(dataPoints.get(1).getBody(), equalTo(expectedBodyMassIndex));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "QkfTizSpRdt6MGLRxULIlVTscmwD_cPJ");
        testProperties.put(HEADER_SCHEMA_ID_KEY, BodyMassIndex.SCHEMA_ID);
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-08-07T05:36:57Z");
        testProperties.put(HEADER_SHARED_KEY, false);
        testDataPointHeader(dataPoints.get(1).getHeader(), testProperties);
    }


}
