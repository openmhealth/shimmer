/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.ginsberg;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.Alcohol;
import org.openmhealth.schema.pojos.build.AlcoholBuilder;
import org.openmhealth.schema.pojos.Journal;
import org.openmhealth.schema.pojos.build.JournalBuilder;
import org.openmhealth.schema.pojos.SleepQuality;
import org.openmhealth.schema.pojos.build.SleepQualityBuilder;
import org.openmhealth.schema.pojos.Tag;
import org.openmhealth.schema.pojos.build.TagBuilder;
import org.openmhealth.schema.pojos.Wellbeing;
import org.openmhealth.schema.pojos.build.WellbeingBuilder;
import org.openmhealth.schema.pojos.WellbeingSurvey;
import org.openmhealth.schema.pojos.build.WellbeingSurveyBuilder;
import org.openmhealth.shim.*;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.*;

/**
 * Encapsulates parameters specific to ginsberg api.
 *
 * @author Fara Kahir
 */
public class GinsbergShim extends OAuth2ShimBase {

    public static final String SHIM_KEY = "ginsberg";

    private static final String DATA_URL = "https://api.ginsberg.io";

    private static final String AUTHORIZE_URL = "https://platform.ginsberg.io/authorisation/auth";

    private static final String TOKEN_URL = "https://platform.ginsberg.io/authorisation/token";

    private GinsbergConfig config;

    public static final ArrayList<String> GINSBERG_SCOPES =
        new ArrayList<String>(Arrays.asList("BasicDemographicRead", "SubjectiveRead",
            "SubjectiveWrite", "ObjectiveRead", "ObjectiveWrite"));

    public GinsbergShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                         AccessParametersRepo accessParametersRepo,
                         ShimServerConfig shimServerConfig1,
                         GinsbergConfig ginsbergConfig) {
        super(authorizationRequestParametersRepo, accessParametersRepo, shimServerConfig1);
        this.config = ginsbergConfig;
    }

    @Override
    public String getLabel() {
        return "Ginsberg";
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getClientSecret() {
        return config.getClientSecret();
    }

    @Override
    public String getClientId() {
        return config.getClientId();
    }

    @Override
    public String getBaseAuthorizeUrl() {
        return AUTHORIZE_URL;
    }

    @Override
    public String getBaseTokenUrl() {
        return TOKEN_URL;
    }

    @Override
    public List<String> getScopes() {
        return GINSBERG_SCOPES;
    }

    public enum GinsbergDataType implements ShimDataType {
        ALCOHOL(
            "v1/o/alcohol",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<Alcohol> alcoholItems = new ArrayList<>();
                    JsonPath alcoholPath = JsonPath.compile("$.[*]");

                    List<Object> alcoholList = JsonPath.read(rawJson, alcoholPath.getPath());
                        if (CollectionUtils.isEmpty(alcoholList)) {
                            return ShimDataResponse.result(GinsbergShim.SHIM_KEY, null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    
                    for (Object gva : alcoholList) {
                    JsonNode gAlcohol = mapper.readTree(((JSONObject) gva).toJSONString());
                    
                    String strTimestamp = gAlcohol.get("timestamp").asText();
                    DateTime timeStamp = new DateTime(strTimestamp);
                    
                    alcoholItems.add(new AlcoholBuilder()
                            .setAlcohol(Double.parseDouble(gAlcohol.get("units").asText()))
                            .setTimeTaken(timeStamp).build());
                    }
                    
                    Map<String, Object> results = new HashMap<>();
                    results.put(Alcohol.SCHEMA_ALCOHOL, alcoholItems);
                    return ShimDataResponse.result(GinsbergShim.SHIM_KEY, results);
                }
            }
        ),  
        JOURNAL(
            "v1/o/events",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<Journal> journalItmes = new ArrayList<>();
                    JsonPath journalPath = JsonPath.compile("$.[*]");

                    List<Object> journalList = JsonPath.read(rawJson, journalPath.getPath());
                        if (CollectionUtils.isEmpty(journalList)) {
                            return ShimDataResponse.result(GinsbergShim.SHIM_KEY, null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    
                    for (Object gva : journalList) {
                    JsonNode gJournal = mapper.readTree(((JSONObject) gva).toJSONString());
                    
                    String entry = gJournal.get("entry").asText();
                    
                    if(!entry.isEmpty()) {
                        String strTimestamp = gJournal.get("timestamp").asText();
                        DateTime timeStamp = new DateTime(strTimestamp);
                        journalItmes.add(new JournalBuilder()
                                .setEntry(entry)
                                .setTimeTaken(timeStamp).build());
                    }
                    
                    }
            
                    Map<String, Object> results = new HashMap<>();
                    results.put(Journal.SCHEMA_JOURNAL, journalItmes);
                    return ShimDataResponse.result(GinsbergShim.SHIM_KEY, results);
                }
            }
        ),  
        SLEEPQUALITY(
            "v1/o/sleep",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<SleepQuality> sleepQualities = new ArrayList<>();
                    JsonPath sleepQualityPath = JsonPath.compile("$.[*]");

                    List<Object> sleepQualityList = JsonPath.read(rawJson, sleepQualityPath.getPath());
                        if (CollectionUtils.isEmpty(sleepQualityList)) {
                            return ShimDataResponse.result(GinsbergShim.SHIM_KEY, null);
                    }
                        
                    ObjectMapper mapper = new ObjectMapper();
                    
                    for (Object gva : sleepQualityList) {
                    JsonNode gSleepQuality = mapper.readTree(((JSONObject) gva).toJSONString());
                    
                    String strTimestamp = gSleepQuality.get("timestamp").asText();
                    DateTime timeStamp = new DateTime(strTimestamp);
                    
                    String quality = gSleepQuality.get("quality").get("value").asText();
                    
                    if(!quality.equals("0")) {
                        sleepQualities.add(new SleepQualityBuilder()
                                .setSleepQuality(quality)
                                .setTimeTaken(timeStamp).build());
                    }
                    
                    }
                    
                    Map<String, Object> results = new HashMap<>();
                    results.put(SleepQuality.SCHEMA_SLEEP_QUALITY, sleepQualities);
                    return ShimDataResponse.result(GinsbergShim.SHIM_KEY, results);
                }
            }
        ),     
        TAG(
            "v1/tags",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<Tag> tags = new ArrayList<>();
                    JsonPath tagPath = JsonPath.compile("$tags.[*]");

                    List<Object> tagList = JsonPath.read(rawJson, tagPath.getPath());
                        if (CollectionUtils.isEmpty(tagList)) {
                            return ShimDataResponse.result(GinsbergShim.SHIM_KEY, null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    
                    for (Object gva : tagList) {
                    JsonNode gTag = mapper.readTree(((JSONObject) gva).toJSONString());
                    
                    tags.add(new TagBuilder()
                            .setTag(gTag.get("tag").asText(), gTag.get("count").asText())
                            .build());
                    }
                    
                    Map<String, Object> results = new HashMap<>();
                    results.put(Tag.SCHEMA_TAG, tags);
                    return ShimDataResponse.result(GinsbergShim.SHIM_KEY, results);
                }
            }
        ),        
        WELLBEING(
            "v1/wellbeing",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<Wellbeing> wellbeingMeasures = new ArrayList<>();
                    JsonPath wellbeingPath = JsonPath.compile("$.[*]");

                    List<Object> wellbeingList = JsonPath.read(rawJson, wellbeingPath.getPath());
                        if (CollectionUtils.isEmpty(wellbeingList)) {
                            return ShimDataResponse.result(GinsbergShim.SHIM_KEY, null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    
                    for (Object gva : wellbeingList) {
                    JsonNode gWellbeing = mapper.readTree(((JSONObject) gva).toJSONString());
                    
                    String strTimestamp = gWellbeing.get("timestamp").asText();
                    DateTime timeStamp = new DateTime(strTimestamp);
                    
                    wellbeingMeasures.add(new WellbeingBuilder()
                            .setWellbeing(gWellbeing.get("value").asText(), gWellbeing.get("measure").asText())
                            .setTimeTaken(timeStamp).build());
                    }
                    
                    Map<String, Object> results = new HashMap<>();
                    results.put(Wellbeing.SCHEMA_WELLBEING, wellbeingMeasures);
                    return ShimDataResponse.result(GinsbergShim.SHIM_KEY, results);
                }
            }
        ),
        WELLBEINGSURVEY(
            "v1/survey/1/answers",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<WellbeingSurvey> wellbeingSurveys = new ArrayList<>();
                    JsonPath wellbeingPath = JsonPath.compile("$.[*]");

                    List<Object> wellbeingSurveyList = JsonPath.read(rawJson, wellbeingPath.getPath());
                        if (CollectionUtils.isEmpty(wellbeingSurveyList)) {
                            return ShimDataResponse.result(GinsbergShim.SHIM_KEY, null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    
                    for (Object gva : wellbeingSurveyList) {
                    JsonNode gWellbeingSurvey = mapper.readTree(((JSONObject) gva).toJSONString());
                    
                    String strTimestamp = gWellbeingSurvey.get("timestamp").asText();
                    DateTime timeStamp = new DateTime(strTimestamp);
                    
                    JsonNode answers = gWellbeingSurvey.get("answers");
                    
                    String satisfaction = "";
                    String cheerfulness = "";
                    String calmness = "";
                    String activeness = "";
                    String freshness = "";
                    String interest = "";
                    
                    for (final JsonNode ansNode : answers) {
                        String question = ansNode.get("question_id").asText();
                        String answer = ansNode.get("value").asText();
                        
                        switch(question)
                        {
                            case "1":
                                satisfaction = answer;
                                break;
                            case "2":
                                cheerfulness = answer;
                                break;
                             case "3":
                                calmness = answer;
                                break;
                            case "4":
                                activeness = answer;
                                break;
                            case "5":
                                freshness = answer;
                                break;
                             case "8":
                                interest = answer;
                                break;                                               
                        }
                    }
                    
                    wellbeingSurveys.add(new WellbeingSurveyBuilder()
                            .setWellbeingSurvey(satisfaction, cheerfulness, calmness, activeness, freshness, interest)
                            .setTimeTaken(timeStamp).build());
                    }
                    
                    Map<String, Object> results = new HashMap<>();
                    results.put(WellbeingSurvey.SCHEMA_WELLBEING_SURVEY, wellbeingSurveys);
                    return ShimDataResponse.result(GinsbergShim.SHIM_KEY, results);
                }
            }
        );

        private String endPointUrl;

        private JsonDeserializer<ShimDataResponse> normalizer;

        GinsbergDataType(String endPointUrl,
                          JsonDeserializer<ShimDataResponse> normalizer) {
            this.endPointUrl = endPointUrl;
            this.normalizer = normalizer;
        }

        @Override
        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return normalizer;
        }

        public String getEndPointUrl() {
            return endPointUrl;
        }
    }

    public AuthorizationCodeAccessTokenProvider getAuthorizationCodeAccessTokenProvider() {
        AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider();
        provider.setTokenRequestEnhancer(new GinsbergTokenRequestEnhancer());
        return provider;
    }

    @Override
    public ShimDataRequest getTriggerDataRequest() {
        ShimDataRequest shimDataRequest = new ShimDataRequest();
        shimDataRequest.setDataTypeKey(GinsbergDataType.WELLBEING.toString());
        shimDataRequest.setNumToReturn(1l);
        return shimDataRequest;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return GinsbergDataType.values();
    }

    protected ResponseEntity<ShimDataResponse> getData(OAuth2RestOperations restTemplate,
                                                       ShimDataRequest shimDataRequest) throws ShimException {

        String dataTypeKey = shimDataRequest.getDataTypeKey().trim().toUpperCase();

        GinsbergDataType ginsbergDataType;
        try {
            ginsbergDataType = GinsbergDataType.valueOf(dataTypeKey);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + dataTypeKey + " in shimDataRequest, cannot retrieve data.");
        }

        String urlRequest = DATA_URL;
        urlRequest += "/" + ginsbergDataType.getEndPointUrl();

        final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        /***
         * Setup default date parameters
         */
        DateTime today = new DateTime();

        DateTime startDate = shimDataRequest.getStartDate() == null ?
            today.minusDays(1) : shimDataRequest.getStartDate();
        String dateStart = startDate.toString(formatter);

        DateTime endDate = shimDataRequest.getEndDate() == null ?
            today.plusDays(1) : shimDataRequest.getEndDate();
        String dateEnd = endDate.toString(formatter);

//        long numToReturn = shimDataRequest.getNumToReturn() == null ||
//            shimDataRequest.getNumToReturn() <= 0 ? 100 :
//            shimDataRequest.getNumToReturn();

//        String urlParams = "";
//
//        urlParams += "&start=" + dateStart;
//        urlParams += "&end=" + dateEnd;
//
//        urlRequest += "".equals(urlParams) ?
//            "" : ("?" + urlParams.substring(1, urlParams.length()));

        ObjectMapper objectMapper = new ObjectMapper();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            urlRequest,
            HttpMethod.GET,
            new HttpEntity<byte[]>(headers),
            byte[].class);


        try {
            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, ginsbergDataType.getNormalizer());
                objectMapper.registerModule(module);
                return new ResponseEntity<>(objectMapper.readValue(response.getBody(),
                    ShimDataResponse.class), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                    ShimDataResponse.result(GinsbergShim.SHIM_KEY, objectMapper.readTree(response.getBody())), HttpStatus.OK);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new ShimException("Could not read response data.");
        }
    }

    protected AuthorizationRequestParameters getAuthorizationRequestParameters(
        final String username,
        final UserRedirectRequiredException exception) {
        final OAuth2ProtectedResourceDetails resource = getResource();
        String authorizationUrl = exception.getRedirectUri()
            + "?state="
            + exception.getStateKey()
            + "&client_id="
            + resource.getClientId()
            + "&response_type=code"
            + "&scope=BasicDemographicRead%20SubjectiveRead%20SubjectiveWrite%20ObjectiveRead%20ObjectiveWrite"
            + "&redirect_uri=" + getCallbackUrl();
        AuthorizationRequestParameters parameters = new AuthorizationRequestParameters();
        parameters.setRedirectUri(exception.getRedirectUri());
        parameters.setStateKey(exception.getStateKey());
        parameters.setHttpMethod(HttpMethod.GET);
        parameters.setAuthorizationUrl(authorizationUrl);
        return parameters;
    }

    /**
     * Adds required parameters to authorization token requests.
     */
    private class GinsbergTokenRequestEnhancer implements RequestEnhancer {
        @Override
        public void enhance(AccessTokenRequest request,
                            OAuth2ProtectedResourceDetails resource,
                            MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("code", request.getAuthorizationCode());
            form.set("client_id", resource.getClientId());
            form.set("grant_type", resource.getGrantType());
            form.set("client_secret", resource.getClientSecret());
        }
    }
}
