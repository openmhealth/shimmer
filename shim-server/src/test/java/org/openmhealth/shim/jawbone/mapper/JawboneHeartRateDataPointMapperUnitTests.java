package org.openmhealth.shim.jawbone.mapper;

import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointModality;
import org.openmhealth.schema.domain.omh.HeartRate;
import org.openmhealth.schema.domain.omh.TemporalRelationshipToPhysicalActivity;
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
import static org.openmhealth.schema.domain.omh.TemporalRelationshipToPhysicalActivity.AT_REST;


/**
 * @author Chris Schaefbauer
 */
public class JawboneHeartRateDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<HeartRate> {

    JawboneHeartRateDataPointMapper mapper = new JawboneHeartRateDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-heartrates.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
        initializeEmptyNode();
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWithEmptyResponse() {

        // FIXME this isn't intuitive, if this is a common test it should be pulled into the parent
        testEmptyNode(mapper);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<HeartRate>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        HeartRate expectedHeartRate = new HeartRate.Builder(55)
                .setTemporalRelationshipToPhysicalActivity(AT_REST)
                .setEffectiveTimeFrame(OffsetDateTime.parse("2013-11-20T08:05:00-08:00"))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedHeartRate));

        // TODO kill maps
        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "40F7_htRRnT8Vo7nRBZO1X");
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2013-11-21T15:59:59Z");
        testProperties.put(HEADER_SCHEMA_ID_KEY, HeartRate.SCHEMA_ID);
        testProperties.put(HEADER_SENSED_KEY, DataPointModality.SENSED);
        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);
    }
}
