package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.openmhealth.shim.common.mapper.JsonNodeMappingException;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.shim.withings.domain.WithingsBodyMeasureType.BODY_HEIGHT;
import static org.openmhealth.shim.withings.mapper.WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class WithingsBodyHeightDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private WithingsBodyHeightDataPointMapper mapper = new WithingsBodyHeightDataPointMapper();
    private JsonNode responseNode;


    @BeforeTest
    public void initializeResponseNode() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("org/openmhealth/shim/withings/mapper/withings-body-measures.json");

        responseNode = objectMapper.readTree(resource.getInputStream());
    }

    // this is included in only one mapper for brevity, can be rinsed and repeated in others if necessary
    @Test(expectedExceptions = JsonNodeMappingException.class)
    public void getValueForMeasureTypeShouldThrowExceptionOnDuplicateMeasureTypes() throws Exception {

        JsonNode measuresNode = objectMapper.readTree("[\n" +
                "    {\n" +
                "        \"type\": 4,\n" + // WithingsBodyMeasureType.BODY_HEIGHT
                "        \"unit\": 0,\n" +
                "        \"value\": 68\n" +
                "    },\n" +
                "    {\n" +
                "        \"type\": 4,\n" +
                "        \"unit\": 0,\n" +
                "        \"value\": 104\n" +
                "    }\n" +
                "]");

        mapper.getValueForMeasureType(measuresNode, BODY_HEIGHT);
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {
        List<DataPoint<BodyHeight>> dataPoints = mapper.asDataPoints(singletonList(responseNode));
        assertThat(dataPoints.size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {
        List<DataPoint<BodyHeight>> actualDataPoints = mapper.asDataPoints(singletonList(responseNode));

        BodyHeight expectedBodyHeight = new BodyHeight.Builder(new LengthUnitValue(LengthUnit.METER, 1.93))
                .setEffectiveTimeFrame(OffsetDateTime.parse("2015-02-23T19:24:49Z"))
                .build();

        assertThat(actualDataPoints.get(0).getBody(), equalTo(expectedBodyHeight));

        DataPointHeader actualDataPointHeader = actualDataPoints.get(0).getHeader();
        assertThat(actualDataPointHeader.getBodySchemaId(), equalTo(BodyHeight.SCHEMA_ID));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getModality(), equalTo(SELF_REPORTED));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));
        assertThat(actualDataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get("external_id"),
                equalTo(320419189L));
    }
}
