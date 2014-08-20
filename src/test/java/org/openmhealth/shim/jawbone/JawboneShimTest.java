package org.openmhealth.shim.jawbone;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.openmhealth.schema.pojos.NumberOfSteps;
import org.openmhealth.schema.pojos.build.NumberOfStepsBuilder;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JawboneShimTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("jawbone-moves.json");
        assert url != null;
        InputStream inputStream = url.openStream();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode responseNode = mapper.readTree(inputStream);

        String rawJson = responseNode.toString();

        List<NumberOfSteps> steps = new ArrayList<>();
        JsonPath stepsPath = JsonPath.compile("$.data.items[*].details.hourly_totals[*]");

        Object hourlyStepTotalsMap = JsonPath.read(rawJson, stepsPath.getPath());

        if (hourlyStepTotalsMap == null) {
            //return ShimDataResponse.empty();
            fail("Test failed, could not parse");
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddhh");

        String jsonString = ((JSONArray) hourlyStepTotalsMap).toJSONString();
        ArrayNode nodes = (ArrayNode) mapper.readTree(jsonString);

        for (Object node1 : nodes) {
            Map<String, JsonNode> jbSteps = mapper.convertValue(node1, HashMap.class);
            for (String timestampStr : jbSteps.keySet()) {

                DateTime dateTime = formatter.parseDateTime(timestampStr);
                Map<String, Object> stepEntry = (Map<String, Object>) jbSteps.get(timestampStr);

                steps.add(new NumberOfStepsBuilder()
                    .setStartTime(dateTime)
                    .setDuration(stepEntry.get("active_time").toString(),
                        DurationUnitValue.DurationUnit.sec.toString())
                    .setSteps((Integer) stepEntry.get("steps")).build());
            }
        }
        Map<String, Object> results = new HashMap<>();
        results.put(NumberOfSteps.SCHEMA_NUMBER_OF_STEPS, steps);
        assertTrue(steps.size() > 0);
        assertTrue(results.size() > 0);
    }
}
