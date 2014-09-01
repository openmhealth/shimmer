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

        //todo: Fix assertions here
    }
}
