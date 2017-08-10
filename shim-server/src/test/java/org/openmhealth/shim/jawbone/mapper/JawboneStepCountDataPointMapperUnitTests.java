package org.openmhealth.shim.jawbone.mapper;

import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointModality;
import org.openmhealth.schema.domain.omh.StepCount1;
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
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Chris Schaefbauer
 */
public class JawboneStepCountDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<StepCount1> {

    private JawboneStepCountDataPointMapper mapper = new JawboneStepCountDataPointMapper();

    @BeforeTest
    public void initializeResponseNodes() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-moves.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
        initializeEmptyNode();
    }

    @Test
    public void asDataPointsShouldReturnNoDataPointsWithEmptyResponse() {

        testEmptyNode(mapper);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<StepCount1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointsForSingleTimeZone() {

        List<DataPoint<StepCount1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        StepCount1 expectedStepCount = new StepCount1.Builder(197)
                .setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2015-08-10T09:16:00-06:00"),
                        OffsetDateTime.parse("2015-08-10T11:43:00-06:00")))
                .build();

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedStepCount));

        Map<String, Object> testProperties = Maps.newHashMap();

        testProperties.put(HEADER_SCHEMA_ID_KEY, StepCount1.SCHEMA_ID);
        testProperties.put(HEADER_SOURCE_UPDATE_KEY, "2015-08-18T03:11:44Z");
        testProperties.put(HEADER_SENSED_KEY, DataPointModality.SENSED);
        testProperties.put(HEADER_EXTERNAL_ID_KEY, "QkfTizSpRdvMvnHFctzItGNZMT-1F5vw");

        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);

    }

    @Test
    public void asDataPointsShouldUseCorrectTimeZoneWhenMultipleTimeZonesOnSingleDay() {

        List<DataPoint<StepCount1>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        StepCount1 expectedStepCount = new StepCount1.Builder(593)
                .setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(
                        OffsetDateTime.parse("2015-08-05T00:00:00-04:00"),
                        OffsetDateTime.parse("2015-08-05T06:42:00-06:00")))
                .build();

        assertThat(dataPoints.get(1).getBody(), equalTo(expectedStepCount));
    }
}
