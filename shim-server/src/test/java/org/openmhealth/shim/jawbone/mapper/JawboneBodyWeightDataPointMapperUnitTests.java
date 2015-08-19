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
    public void initializeResponseNode() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/jawbone/mapper/jawbone-body-events.json");
        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(2));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<BodyWeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyWeight testBodyWeight = dataPoints.get(0).getBody();
        BodyWeight expectedBodyWeight = new BodyWeight.
                Builder(new MassUnitValue(MassUnit.KILOGRAM, 86.0010436535)).
                setEffectiveTimeFrame(OffsetDateTime.parse("2015-08-11T22:37:20-06:00")).
                setUserNotes("First weight").
                build();

        assertThat(testBodyWeight, equalTo(expectedBodyWeight));

        Map<String, Object> testProperties = Maps.newHashMap();
        testProperties.put("schemaId", BodyWeight.SCHEMA_ID);
        testProperties.put("externalId", "QkfTizSpRdubVXHaj3iZk6Vd3qqdl_RJ");
        testProperties.put("sourceUpdatedDateTime", "2015-08-12T04:37:20Z");
        testProperties.put("shared", false);

        testDataPointHeader(dataPoints.get(0).getHeader(), testProperties);

        testBodyWeight = dataPoints.get(1).getBody();
        expectedBodyWeight = new BodyWeight.Builder(new MassUnitValue(MassUnit.KILOGRAM, 86.5010436535))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-08-11T22:37:18-06:00"))
                .build();
        assertThat(testBodyWeight,equalTo(expectedBodyWeight));

        testProperties = Maps.newHashMap();
        testProperties.put("schemaId",BodyWeight.SCHEMA_ID);
        testProperties.put("externalId","QkfTizSpRdukQY3ns4PYbkucZTM5yPMg");
        testProperties.put("sourceUpdatedDateTime","2015-08-13T08:23:58Z");
        testProperties.put("shared",true);
        testDataPointHeader(dataPoints.get(1).getHeader(), testProperties);

    }


}
