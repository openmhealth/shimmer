package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;
import org.junit.Test;
import org.openmhealth.schema.pojos.NumberOfSteps;
import org.openmhealth.schema.pojos.build.NumberOfStepsBuilder;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.shim.ShimDataResponse;
import org.openmhealth.shim.healthvault.HealthvaultShim;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FitbitShimTest {


    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("fitbit-activities-1m.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode responseNode = mapper.readTree(inputStream);

        String rawJson = responseNode.toString();

        List<NumberOfSteps> steps = new ArrayList<>();
        JsonPath stepsPath = JsonPath.compile("$.[*].result.content[*]");

        Object oneMinStepEntries = JsonPath.read(rawJson, stepsPath.getPath());

        if (oneMinStepEntries == null) {
            //return ShimDataResponse.empty();
            fail("Test failed, could not parse");
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        String jsonString = ((JSONArray) oneMinStepEntries).toJSONString();
        ArrayNode nodes = (ArrayNode) mapper.readTree(jsonString);

        for (Object node1 : nodes) {
            JsonNode fbStepNode = (JsonNode) node1;

            String dateString =
                (fbStepNode.get("activities-steps")).get(0).get("dateTime").asText();

            ArrayNode dataset = (ArrayNode)
                fbStepNode.get("activities-steps-intraday").get("dataset");

            for (JsonNode stepMinute : dataset) {
                if (stepMinute.get("value").asInt() > 0) {
                    steps.add(new NumberOfStepsBuilder()
                        .setStartTime(formatter.parseDateTime(
                            dateString + " " + stepMinute.get("time").asText()))
                        .setDuration("1", DurationUnitValue.DurationUnit.min.toString())
                        .setSteps(stepMinute.get("value").asInt())
                        .build());
                }
            }
        }
        Map<String, Object> results = new HashMap<>();
        results.put(NumberOfSteps.SCHEMA_NUMBER_OF_STEPS, steps);
        assertTrue(steps.size() > 0);
        assertTrue(results.size() > 0);
    }

    @Test
    @Ignore
    public void testXml() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class,
            HealthvaultShim.HealthVaultDataType.WEIGHT.getNormalizer());
        xmlMapper.registerModule(module);

        URL url = Thread.currentThread().getContextClassLoader().getResource("data-response-weight.xml");
        assert url != null;
        InputStream inputStream = url.openStream();

        ShimDataResponse response = xmlMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(response.getBody());

        inputStream.close();
    }

    @Test
    @Ignore
    public void testConvert() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(ShimDataResponse.class, FitbitShim.FitbitDataType.ACTIVITY.getNormalizer());
        objectMapper.registerModule(module);

        URL url = Thread.currentThread().getContextClassLoader().getResource("data-response-activity.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ShimDataResponse shimDataResponse = objectMapper.readValue(inputStream, ShimDataResponse.class);

        assertNotNull(shimDataResponse.getBody());

        inputStream.close();
    }
}
