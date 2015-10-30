package org.openmhealth.shim.jawbone.mapper;

import com.google.common.collect.Maps;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.MassUnit;
import org.openmhealth.schema.domain.omh.MassUnitValue;
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
public class JawboneBodyWeightDataPointMapperUnitTests extends JawboneDataPointMapperUnitTests<BodyWeight> {

    JawboneBodyWeightDataPointMapper mapper = new JawboneBodyWeightDataPointMapper();

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

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(3));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointWhenTimeZoneIsInOlsonFormat() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyWeight testBodyWeight = dataPoints.get(0).getBody();
        BodyWeight expectedBodyWeight = new BodyWeight.
                Builder(new MassUnitValue(MassUnit.KILOGRAM, 64)).
                setEffectiveTimeFrame(OffsetDateTime.parse("2015-10-04T19:52:41-06:00")).
                setUserNotes("First weight").
                build();

        assertThat(testBodyWeight, equalTo(expectedBodyWeight));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put("schemaId", BodyWeight.SCHEMA_ID);
        testProperties.put("externalId", "JM2JlMHcHlVJUd597YY3Lnny5eEku5Ll");
        testProperties.put("sourceUpdatedDateTime", "2015-10-06T01:52:51Z");
        testProperties.put("shared", false);

        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPointWhenTimeZoneIsInGMTOffset() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        BodyWeight testBodyWeight = dataPoints.get(1).getBody();
        BodyWeight expectedBodyWeight = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, 74.5))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-10-05T19:52:52-06:00"))
                .build();
        assertThat(testBodyWeight, equalTo(expectedBodyWeight));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put("schemaId", BodyWeight.SCHEMA_ID);
        testProperties.put("externalId", "JM2JlMHcHlUP2mAvWWVlwwNFFVo_4CfQ");
        testProperties.put("sourceUpdatedDateTime", "2015-10-06T01:52:52Z");
        testProperties.put("shared", true);
        testDataPointHeader(dataPoints.get(1).getHeader(), testProperties);
    }

    @Test
    public void asDataPointsShouldReturnDataPointWithoutEffectiveTimeFrameWhenTimeZoneIsNull(){

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyWeight expectedBodyWeight = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, 74.8))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-10-06T19:39:01Z"))
                .build();

        assertThat(dataPoints.get(2).getBody(),equalTo(expectedBodyWeight));
    }


}
