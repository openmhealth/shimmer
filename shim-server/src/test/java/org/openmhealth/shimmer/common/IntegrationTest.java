package org.openmhealth.shimmer.common;

/**
 * Created by wwadge on 19/09/2016.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmhealth.shim.AccessParameters;
import org.openmhealth.shim.AccessParametersRepo;
import org.openmhealth.shim.ShimDataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openmhealth.schema.configuration.JacksonConfiguration.newObjectMapper;

/**
 * A suite of integration tests
 *
 * @author Wallace Wadge
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebMvcTest(LegacyDataPointSearchController.class)
public class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    OAuth2RestTemplate oAuth2RestTemplate;


    @TestConfiguration
    public static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return newObjectMapper();
        }


    }

    @Autowired
    AccessParametersRepo accessParametersRepo;


    @Test
    public void msMap() throws IOException {

        AccessParameters accessParameters = new AccessParameters();
        accessParameters.setUsername("test");
        accessParameters.setShimKey("microsoft");
        accessParameters.setStateKey("garbage");
        accessParametersRepo.save(accessParameters);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree("{\n" +
                "    \"runActivities\": [\n" +
                "      {\n" +
                "        \"activityType\": \"Run\",\n" +
                "        \"exerciseTypeName\": \"Run\",\n" +
                "        \"performanceSummary\": {\n" +
                "          \"heartRateZones\": {\n" +
                "            \"underHealthyHeart\": 75,\n" +
                "            \"underAerobic\": 75,\n" +
                "            \"aerobic\": 1,\n" +
                "            \"anaerobic\": 1\n" +
                "          }\n" +
                "        },\n" +
                "        \"distanceSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalDistance\": 505063,\n" +
                "          \"actualDistance\": 505063,\n" +
                "          \"elevationGain\": 3500,\n" +
                "          \"elevationLoss\": 2400,\n" +
                "          \"maxElevation\": 3800,\n" +
                "          \"minElevation\": 1500,\n" +
                "          \"waypointDistance\": 2500,\n" +
                "          \"pace\": 906610\n" +
                "        },\n" +
                "        \"splitDistance\": 160934,\n" +
                "        \"id\": \"2519379974844943315\",\n" +
                "        \"startTime\": \"2016-05-26T15:55:15.505-07:00\",\n" +
                "        \"endTime\": \"2016-05-26T17:11:34.505-07:00\",\n" +
                "        \"dayId\": \"2016-05-26T00:00:00.000+00:00\",\n" +
                "        \"duration\": \"PT1H16M18S\",\n" +
                "        \"caloriesBurnedSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalCalories\": 325\n" +
                "        },\n" +
                "        \"heartRateSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"averageHeartRate\": 144,\n" +
                "          \"peakHeartRate\": 145,\n" +
                "          \"lowestHeartRate\": 130\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"activityType\": \"Run\",\n" +
                "        \"exerciseTypeName\": \"Run\",\n" +
                "        \"performanceSummary\": {\n" +
                "          \"heartRateZones\": {\n" +
                "            \"underHealthyHeart\": 4,\n" +
                "            \"underAerobic\": 4\n" +
                "          }\n" +
                "        },\n" +
                "        \"distanceSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalDistance\": 70956,\n" +
                "          \"actualDistance\": 70956,\n" +
                "          \"waypointDistance\": 2500,\n" +
                "          \"pace\": 231485\n" +
                "        },\n" +
                "        \"splitDistance\": 160934,\n" +
                "        \"id\": \"2519387108429300555\",\n" +
                "        \"startTime\": \"2016-05-18T09:45:57.069-07:00\",\n" +
                "        \"endTime\": \"2016-05-18T09:48:42.069-07:00\",\n" +
                "        \"dayId\": \"2016-05-18T00:00:00.000+00:00\",\n" +
                "        \"duration\": \"PT2M44S\",\n" +
                "        \"caloriesBurnedSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalCalories\": 6\n" +
                "        },\n" +
                "        \"heartRateSummary\": {\n" +
                "          \"period\": \"Activity\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"activityType\": \"Run\",\n" +
                "        \"exerciseTypeName\": \"Run\",\n" +
                "        \"performanceSummary\": {\n" +
                "          \"heartRateZones\": {\n" +
                "            \"underHealthyHeart\": 16,\n" +
                "            \"underAerobic\": 16\n" +
                "          }\n" +
                "        },\n" +
                "        \"distanceSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalDistance\": 153112,\n" +
                "          \"actualDistance\": 200240,\n" +
                "          \"elevationGain\": 400,\n" +
                "          \"elevationLoss\": 2800,\n" +
                "          \"maxElevation\": 4500,\n" +
                "          \"minElevation\": 1800,\n" +
                "          \"waypointDistance\": 2500,\n" +
                "          \"pace\": 377270\n" +
                "        },\n" +
                "        \"splitDistance\": 160934,\n" +
                "        \"id\": \"2519389648412546121\",\n" +
                "        \"startTime\": \"2016-05-15T11:12:38.745-07:00\",\n" +
                "        \"endTime\": \"2016-05-15T11:27:23.745-07:00\",\n" +
                "        \"dayId\": \"2016-05-15T00:00:00.000+00:00\",\n" +
                "        \"duration\": \"PT9M37S\",\n" +
                "        \"caloriesBurnedSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalCalories\": 111\n" +
                "        },\n" +
                "        \"heartRateSummary\": {\n" +
                "          \"period\": \"Activity\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"activityType\": \"Run\",\n" +
                "        \"exerciseTypeName\": \"Run\",\n" +
                "        \"performanceSummary\": {\n" +
                "          \"heartRateZones\": {\n" +
                "            \"underHealthyHeart\": 6,\n" +
                "            \"underAerobic\": 6\n" +
                "          }\n" +
                "        },\n" +
                "        \"distanceSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalDistance\": 117011,\n" +
                "          \"actualDistance\": 117701,\n" +
                "          \"elevationGain\": 200,\n" +
                "          \"elevationLoss\": 0,\n" +
                "          \"maxElevation\": 3700,\n" +
                "          \"minElevation\": 3500,\n" +
                "          \"waypointDistance\": 2500,\n" +
                "          \"pace\": 273953\n" +
                "        },\n" +
                "        \"splitDistance\": 160934,\n" +
                "        \"id\": \"2519389652877727203\",\n" +
                "        \"startTime\": \"2016-05-15T11:05:12.227-07:00\",\n" +
                "        \"endTime\": \"2016-05-15T11:10:40.227-07:00\",\n" +
                "        \"dayId\": \"2016-05-15T00:00:00.000+00:00\",\n" +
                "        \"duration\": \"PT5M20S\",\n" +
                "        \"caloriesBurnedSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalCalories\": 14\n" +
                "        },\n" +
                "        \"heartRateSummary\": {\n" +
                "          \"period\": \"Activity\"\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"sleepActivities\": [\n" +
                "      {\n" +
                "        \"activityType\": \"Sleep\",\n" +
                "        \"awakeDuration\": \"PT40M25S\",\n" +
                "        \"sleepDuration\": \"PT6H45M43S\",\n" +
                "        \"numberOfWakeups\": 5,\n" +
                "        \"fallAsleepDuration\": \"PT4M59S\",\n" +
                "        \"sleepEfficiencyPercentage\": 92,\n" +
                "        \"totalRestlessSleepDuration\": \"PT5H41M52S\",\n" +
                "        \"totalRestfulSleepDuration\": \"PT1H3M51S\",\n" +
                "        \"restingHeartRate\": 53,\n" +
                "        \"fallAsleepTime\": \"2016-05-16T06:52:55.148+00:00\",\n" +
                "        \"wakeupTime\": \"2016-05-16T14:11:35.751+00:00\",\n" +
                "        \"id\": \"2519389195238519452\",\n" +
                "        \"startTime\": \"2016-05-15T23:47:56.148-07:00\",\n" +
                "        \"endTime\": \"2016-05-16T07:14:05.148-07:00\",\n" +
                "        \"dayId\": \"2016-05-15T00:00:00.000+00:00\",\n" +
                "        \"duration\": \"PT7H26M9S\",\n" +
                "        \"caloriesBurnedSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalCalories\": 381\n" +
                "        },\n" +
                "        \"heartRateSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"averageHeartRate\": 62,\n" +
                "          \"peakHeartRate\": 92,\n" +
                "          \"lowestHeartRate\": 51\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"activityType\": \"Sleep\",\n" +
                "        \"awakeDuration\": \"PT59M19S\",\n" +
                "        \"sleepDuration\": \"PT8H15M31S\",\n" +
                "        \"numberOfWakeups\": 9,\n" +
                "        \"fallAsleepDuration\": \"PT3M29S\",\n" +
                "        \"sleepEfficiencyPercentage\": 90,\n" +
                "        \"totalRestlessSleepDuration\": \"PT6H40M20S\",\n" +
                "        \"totalRestfulSleepDuration\": \"PT1H35M11S\",\n" +
                "        \"restingHeartRate\": 50,\n" +
                "        \"fallAsleepTime\": \"2016-05-15T07:01:25.860+00:00\",\n" +
                "        \"wakeupTime\": \"2016-05-15T16:10:18.097+00:00\",\n" +
                "        \"id\": \"2519390053231391562\",\n" +
                "        \"startTime\": \"2016-05-14T23:57:56.860-07:00\",\n" +
                "        \"endTime\": \"2016-05-15T09:12:46.860-07:00\",\n" +
                "        \"dayId\": \"2016-05-14T00:00:00.000+00:00\",\n" +
                "        \"duration\": \"PT9H14M50S\",\n" +
                "        \"caloriesBurnedSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"totalCalories\": 462\n" +
                "        },\n" +
                "        \"heartRateSummary\": {\n" +
                "          \"period\": \"Activity\",\n" +
                "          \"averageHeartRate\": 58,\n" +
                "          \"peakHeartRate\": 78,\n" +
                "          \"lowestHeartRate\": 47\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"itemCount\": 6\n" +
                "  }\n" +
                "");

        when(oAuth2RestTemplate.getForEntity(any(), any())).thenReturn(new ResponseEntity<>(actualObj, HttpStatus.OK));

        ResponseEntity<ShimDataResponse> response = this.restTemplate.getForEntity(
                "/data/microsoft/ACTIVITY?username=test", ShimDataResponse.class);


        // FIXME: Figure out how to properly map this back to our omh schema
        Object header = ((Map<String, Object>) ((List) ((ShimDataResponse) ((ResponseEntity) response).getBody()).getBody()).get(0)).get("header");
        Object schema = ((Map<String, String>) header).get("schema_id");
        assertEquals("physical-activity", ((Map<String, String>) schema).get("name"));

    }

}
