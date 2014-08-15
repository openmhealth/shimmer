package org.openmhealth.shim.healthvault;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jayway.jsonpath.JsonPath;
import com.microsoft.hsg.*;
import net.minidev.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.build.*;
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.generic.DurationUnitValue;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Notes the healthvault shim is neither OAuth1.0/2.0, it's
 * a custom implementation.
 */
public class HealthvaultShim implements Shim {

    public static final String SHIM_KEY = "healthvault";

    public static final String CLIENT_ID = "bafb1313-d4e0-421c-b3b5-4e3a55639c19";

    private static final String AUTHORIZE_URL = "https://account.healthvault-ppe.com";

    private static final String ACTION_QS = "/";

    public static final String TOKEN_PARAM = "wctoken";

    public static final String RECORD_ID_PARAM = "recordId";

    private Connection connection = ConnectionFactory.getConnection();

    private static DateTimeFormatter formatterMins = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static DateTimeFormatter formatterDate = DateTimeFormat.forPattern("yyyy-MM-dd");

    private AuthorizationRequestParametersRepo authorizationRequestParametersRepo;

    private ShimServerConfig shimServerConfig;

    public HealthvaultShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                           ShimServerConfig shimServerConfig) {
        this.authorizationRequestParametersRepo = authorizationRequestParametersRepo;
        this.shimServerConfig = shimServerConfig;
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getClientId() {
        return CLIENT_ID;
    }

    @Override
    public String getBaseAuthorizeUrl() {
        return AUTHORIZE_URL;
    }

    @Override
    public ShimDataType[] getShimDataTypes() {
        return HealthVaultDataType.values();
    }

    public enum HealthVaultDataType implements ShimDataType {

        ACTIVITY(
            "85a21ddb-db20-4c65-8d30-33c899ccf612",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<Activity> activities = new ArrayList<>();
                    List<NumberOfSteps> numberOfStepsList = new ArrayList<>();

                    JsonPath activityPath = JsonPath.compile("$.things[*].data-xml.aerobic-session");

                    final List<Object> hvActivities = JsonPath.read(rawJson, activityPath.getPath());
                    if (CollectionUtils.isEmpty(hvActivities)) {
                        return ShimDataResponse.result(null);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : hvActivities) {
                        final JsonNode hvActivity = mapper.readTree(((JSONObject) fva).toJSONString());

                        DateTime startTime = parseDateTimeFromWhenNode(hvActivity.get("when"));

                        JsonNode sessionNode = hvActivity.get("session");

                        Activity activity = new ActivityBuilder()
                            .setActivityName(sessionNode.get("mode").get("text").asText())
                            .setDistance(
                                sessionNode.get("distance").get("display").get("").asText(),
                                LengthUnitValue.LengthUnit.m.toString())
                            .setDuration(
                                sessionNode.get("minutes").asText(),
                                DurationUnitValue.DurationUnit.min.toString())
                            .setStartTime(startTime).build();

                        NumberOfSteps numberOfSteps = new NumberOfStepsBuilder()
                            .setSteps(sessionNode.get("number-of-steps").asInt()).build();
                        numberOfSteps.setEffectiveTimeFrame(activity.getEffectiveTimeFrame());

                        activities.add(activity);
                        numberOfStepsList.add(numberOfSteps);
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(Activity.SCHEMA_ACTIVITY, activities);
                    results.put(NumberOfSteps.SCHEMA_NUMBER_OF_STEPS, numberOfStepsList);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        BLOOD_PRESSURE(
            "ca3c57f4-f4c1-4e15-be67-0a3caf5414ed",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext ctxt)
                    throws IOException {

                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BloodPressure> bloodPressures = new ArrayList<>();
                    List<HeartRate> heartRates = new ArrayList<>();
                    JsonPath bloodPressurePath = JsonPath.compile("$.things[*].data-xml.blood-pressure");

                    List<Object> hvBloodPressures = JsonPath.read(rawJson, bloodPressurePath.getPath());
                    if (CollectionUtils.isEmpty(hvBloodPressures)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : hvBloodPressures) {
                        JsonNode hvBloodPressure = mapper.readTree(((JSONObject) fva).toJSONString());

                        DateTime dateTimeWhen =
                            parseDateTimeFromWhenNode(hvBloodPressure.get("when"));

                        bloodPressures.add(new BloodPressureBuilder()
                            .setTimeTaken(dateTimeWhen)
                            .setValues(
                                new BigDecimal(hvBloodPressure.get("systolic").asText()),
                                new BigDecimal(hvBloodPressure.get("diastolic").asText())
                            ).build());

                        heartRates.add(new HeartRateBuilder()
                            .setRate(hvBloodPressure.get("pulse").asText())
                            .setTimeTaken(dateTimeWhen).build());
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BloodPressure.SCHEMA_BLOOD_PRESSURE, bloodPressures);
                    results.put(HeartRate.SCHEMA_HEART_RATE, heartRates);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        HEIGHT(
            "40750a6a-89b2-455c-bd8d-b420a4cb500b",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BodyHeight> bodyHeights = new ArrayList<>();
                    JsonPath bodyHeightsPath = JsonPath.compile("$.things[*].data-xml.height");

                    List<Object> hvHeights = JsonPath.read(rawJson, bodyHeightsPath.getPath());
                    if (CollectionUtils.isEmpty(hvHeights)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : hvHeights) {
                        JsonNode hvHeight = mapper.readTree(((JSONObject) fva).toJSONString());

                        DateTime dateTimeWhen = parseDateTimeFromWhenNode(hvHeight.get("when"));

                        BodyHeight bodyHeight = new BodyHeightBuilder()
                            .setHeight(
                                hvHeight.get("value").get("display").get("").asText(),
                                LengthUnitValue.LengthUnit.in.toString())
                            .setTimeTaken(dateTimeWhen).build();

                        bodyHeights.add(bodyHeight);
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BodyHeight.SCHEMA_BODY_HEIGHT, bodyHeights);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        BLOOD_GLUCOSE(
            "879e7c04-4e8a-4707-9ad3-b054df467ce4",
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {
                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BloodGlucose> bloodGlucoses = new ArrayList<>();
                    JsonPath bloodGlucosePath = JsonPath.compile("$.things[*].data-xml.blood-glucose");

                    List<Object> hvbloodGlucoses = JsonPath.read(rawJson, bloodGlucosePath.getPath());
                    if (CollectionUtils.isEmpty(hvbloodGlucoses)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : hvbloodGlucoses) {
                        JsonNode hvBloodGlucose = mapper.readTree(((JSONObject) fva).toJSONString());

                        DateTime dateTimeWhen =
                            parseDateTimeFromWhenNode(hvBloodGlucose.get("when"));

                        String hvMeasureType = hvBloodGlucose.get("glucose-measurement-type").get("text").asText();
                        hvMeasureType = hvMeasureType.toLowerCase().replaceAll(" ", "_");

                        /**
                         * Must parse out the meal context properly, HV has many
                         * available choices, we care about fewer.
                         */
                        BloodGlucose.MealContext mealContext = null;
                        if (hvBloodGlucose.get("measurement-context") != null) {
                            String hvMealContext = hvBloodGlucose.get("measurement-context").get("text").asText();
                            hvMealContext = hvMealContext.toLowerCase().trim();
                            if (hvMealContext.contains("after")
                                && !hvMealContext.contains("exercise")) {
                                mealContext = BloodGlucose.MealContext.after_meal;
                            } else if (hvMealContext.contains("before")
                                && !hvMealContext.contains("exercise")) {
                                mealContext = BloodGlucose.MealContext.before_meal;
                            } else if (hvMealContext.startsWith("non")) {
                                mealContext = BloodGlucose.MealContext.not_fasting;
                            } else if (hvMealContext.contains("fasting")) {
                                mealContext = BloodGlucose.MealContext.fasting;
                            }
                        }

                        bloodGlucoses.add(new BloodGlucoseBuilder()
                            .setTimeTaken(dateTimeWhen)
                            .setValue(hvBloodGlucose.get("value").
                                get("display").get("").asText())
                            .setMeasureContext(hvMeasureType)
                            .setMealContext(mealContext != null ? mealContext.toString() : null)
                            .build());
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BloodGlucose.SCHEMA_BLOOD_GLUCOSE, bloodGlucoses);
                    return ShimDataResponse.result(results);
                }
            }
        ),

        WEIGHT(
            "3d34d87e-7fc1-4153-800f-f56592cb0d17",
            new JsonDeserializer<ShimDataResponse>() {

                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser, DeserializationContext ctxt)
                    throws IOException {

                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BodyWeight> bodyWeights = new ArrayList<>();
                    JsonPath bodyWeightsPath = JsonPath.compile("$.things[*].data-xml.weight");

                    List<Object> hvWeights = JsonPath.read(rawJson, bodyWeightsPath.getPath());
                    if (CollectionUtils.isEmpty(hvWeights)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object fva : hvWeights) {
                        JsonNode hvWeight = mapper.readTree(((JSONObject) fva).toJSONString());

                        DateTime dateTimeWhen = parseDateTimeFromWhenNode(hvWeight.get("when"));

                        BodyWeight bodyWeight = new BodyWeightBuilder()
                            .setWeight(
                                hvWeight.get("value").get("display").get("").asText(),
                                MassUnitValue.MassUnit.lb.toString())
                            .setTimeTaken(dateTimeWhen).build();

                        bodyWeights.add(bodyWeight);
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BodyWeight.SCHEMA_BODY_WEIGHT, bodyWeights);
                    return ShimDataResponse.result(results);
                }
            });

        private String dataTypeId;

        private JsonDeserializer<ShimDataResponse> normalizer;

        HealthVaultDataType(String dataTypeId, JsonDeserializer<ShimDataResponse> normalizer) {
            this.dataTypeId = dataTypeId;
            this.normalizer = normalizer;
        }

        public String getDataTypeId() {
            return dataTypeId;
        }

        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return normalizer;
        }
    }

    @Override
    public ShimDataResponse getData(final ShimDataRequest shimDataRequest) throws ShimException {
        final HealthVaultDataType healthVaultDataType;
        try {
            healthVaultDataType = HealthVaultDataType.valueOf(
                shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'hh:mm:ss");

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

        long numToReturn = shimDataRequest.getNumToReturn() == null ||
            shimDataRequest.getNumToReturn() <= 0 ? 100 :
            shimDataRequest.getNumToReturn();

        Request request = new Request();
        request.setMethodName("GetThings");
        request.setInfo(
            "<info>" +
                "<group max=\"" + numToReturn + "\">" +
                "<filter>" +
                "<type-id>" + healthVaultDataType.getDataTypeId() + "</type-id>" +
                "<eff-date-min>" + dateStart + "</eff-date-min>" +
                "<eff-date-max>" + dateEnd + "</eff-date-max>" +
                "</filter>" +
                "<format>" +
                "<section>core</section>" +
                "<xml/>" +
                "</format>" +
                "</group>" +
                "</info>");

        RequestTemplate template = new RequestTemplate(connection);
        return template.makeRequest(shimDataRequest.getAccessParameters(),
            request, new Marshaller<ShimDataResponse>() {
                public ShimDataResponse marshal(InputStream istream) throws Exception {

                    /**
                     * XML Document mappings to JSON don't respect repeatable
                     * tags, they don't get properly serialized into collections.
                     * Thus, we pickup the 'things' via the 'group' root tag
                     * and create a new JSON document out of each 'thing' node.
                     */
                    XmlMapper xmlMapper = new XmlMapper();
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(istream);
                    NodeList nodeList = doc.getElementsByTagName("thing");

                    /**
                     * Collect JsonNode from each 'thing' xml node.
                     */
                    List<JsonNode> thingList = new ArrayList<>();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        Document thingDoc = builder.newDocument();
                        Node newNode = thingDoc.importNode(node, true);
                        thingDoc.appendChild(newNode);
                        thingList.add(xmlMapper.readTree(convertDocumentToString(thingDoc)));
                    }

                    /**
                     * Rebuild JSON document structure to pass to deserializer.
                     */
                    String thingsJson = "{\"things\":[";
                    String thingsContent = "";
                    for (JsonNode thingNode : thingList) {
                        thingsContent += thingNode.toString() + ",";
                    }
                    thingsContent = "".equals(thingsContent) ? thingsContent :
                        thingsContent.substring(0, thingsContent.length() - 1);
                    thingsJson += thingsContent;
                    thingsJson += "]}";

                    /**
                     * Return raw re-built 'things' or a normalized JSON document.
                     */
                    ObjectMapper objectMapper = new ObjectMapper();
                    if (shimDataRequest.getNormalize()) {
                        SimpleModule module = new SimpleModule();
                        module.addDeserializer(ShimDataResponse.class, healthVaultDataType.getNormalizer());
                        objectMapper.registerModule(module);
                        return objectMapper.readValue(thingsJson, ShimDataResponse.class);
                    } else {
                        return ShimDataResponse.result(objectMapper.readTree(thingsJson));
                    }
                }
            });
    }

    /**
     * Utility method for getting XML fragments required
     * for parsing XML docs from microsoft.
     *
     * @param doc - XML document fragment.
     * @return - Raw XML String.
     */
    private static String convertDocumentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public AuthorizationRequestParameters getAuthorizationRequestParameters(
        String username, Map<String, String> addlParameters) throws ShimException {

        String stateKey = OAuth1Utils.generateStateKey();
        AuthorizationRequestParameters authParams = new AuthorizationRequestParameters();
        authParams.setUsername(username);
        authParams.setStateKey(stateKey);

        //Callback URL
        String callbackUrl = shimServerConfig.getCallbackUrl(getShimKey(), stateKey);
        authParams.setAuthorizationUrl(getAuthorizationUrl(callbackUrl, ACTION_QS));

        authorizationRequestParametersRepo.save(authParams);

        return authParams;
    }

    @Override
    public AuthorizationResponse handleAuthorizationResponse(
        HttpServletRequest servletRequest) throws ShimException {

        String stateKey = servletRequest.getParameter("state");

        AuthorizationRequestParameters authParams =
            authorizationRequestParametersRepo.findByStateKey(stateKey);
        if (authParams == null) {
            throw new ShimException("Invalid state, could not find " +
                "corresponding auth parameters");
        }

        // Fetch the access token.
        String accessToken = servletRequest.getParameter(TOKEN_PARAM);
        final String recordId = getSelectedRecordId(accessToken);

        AccessParameters accessParameters = new AccessParameters();
        accessParameters.setClientId(getClientId());
        accessParameters.setStateKey(stateKey);
        accessParameters.setUsername(authParams.getUsername());
        accessParameters.setAccessToken(accessToken);
        accessParameters.setAdditionalParameters(new HashMap<String, Object>() {{
            put(RECORD_ID_PARAM, recordId);
        }});

        return AuthorizationResponse.authorized(accessParameters);
    }

    private String getAuthorizationUrl(String redirectUrl, String actionQs) {
        return getBaseAuthorizeUrl()
            + "/redirect.aspx?target=AUTH&targetqs=?appid=" + getClientId()
            + "%26redirect=" + redirectUrl
            + "%26actionqs=" + actionQs;
    }

    /**
     * Retrieves specific recordId to look at in HealthVault.
     * Can be retrieved with the given authentication token
     * and is required on all subsequent requests.
     *
     * @param userAuthToken - User authentication token (i.e., wcToken)
     * @return - String representing the record id for a person.
     * @throws HVException
     */
    private String getSelectedRecordId(String userAuthToken)
        throws HVException {
        try {
            Request request = new Request();
            request.setTtl(3600 * 8 + 300);
            request.setMethodName("GetPersonInfo");
            request.setUserAuthToken(userAuthToken);
            HVAccessor accessor = new HVAccessor();
            accessor.send(request, ConnectionFactory.getConnection());
            InputStream is = accessor.getResponse().getInputStream();

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "//record/@id";
            return xpath.evaluate(exp, new InputSource(is));
        } catch (HVException he) {
            throw he;
        } catch (Exception e) {
            throw new HVException(e);
        }
    }

    /**
     * Utility for parsing a dateTime from 'when' xml node from
     * health vault.
     *
     * @param whenNode - The 'when' node of a 'thing' document.
     * @return - Joda DateTime corresponding to when node.
     */
    private static DateTime parseDateTimeFromWhenNode(JsonNode whenNode) {
        if (whenNode == null) {
            return null;
        }
        JsonNode dateNode = whenNode.get("date");
        JsonNode timeNode = whenNode.get("time");
        String dateString = dateNode.get("y").asText()
            + "-" + dateNode.get("m").asText() + "-" + dateNode.get("d").asText();
        if (timeNode != null) {
            String timeString = timeNode.get("h").asText() + ":" + timeNode.get("m").asText();
            return formatterMins.parseDateTime(dateString + " " + timeString);
        } else {
            return formatterDate.parseDateTime(dateString);
        }
    }

    /**
     * Utility interface for marshalling responses
     * from healthvault.
     *
     * @param <T>
     */
    private interface Marshaller<T> {
        T marshal(InputStream is) throws Exception;
    }

    /**
     * Utility class for making requests to healthvault.
     */
    private class RequestTemplate {
        Connection connection;

        public RequestTemplate(Connection connection) {
            this.connection = connection;
        }

        public Integer makeRequest(AccessParameters accessParameters, Request request) {
            return makeRequest(accessParameters, request, new Marshaller<Integer>() {
                public Integer marshal(InputStream is) {
                    return 0;
                }
            });
        }

        public <T> T makeRequest(AccessParameters accessInfo,
                                 Request request, Marshaller<T> marshaller) {
            request.setTtl(3600 * 8 + 300);
            request.setUserAuthToken(accessInfo.getAccessToken());
            request.setRecordId(
                accessInfo.getAdditionalParameters().get(RECORD_ID_PARAM).toString());
            HVAccessor accessor = new HVAccessor();
            accessor.send(request, ConnectionFactory.getConnection());
            try {
                try (InputStream istream = accessor.getResponse().getInputStream()) {
                    return marshaller.marshal(istream);
                }
            } catch (HVException e) {
                throw e;
            } catch (Exception e) {
                //TODO: need exception translator
                throw new HVException("Could not marshal response", e);
            }
        }
    }

    @Override
    public String getClientSecret() {
        return null; //NOOP
    }

    @Override
    public String getBaseTokenUrl() {
        return null; //NOOP
    }

    @Override
    public List<String> getScopes() {
        return null; //NOOP
    }
}
