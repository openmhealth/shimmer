package org.openmhealth.shim.healthvault;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jayway.jsonpath.JsonPath;
import com.microsoft.hsg.*;
import net.minidev.json.JSONObject;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.BodyWeight;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.shim.*;
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

    private static Map<String, AuthorizationRequestParameters> AUTH_PARAMS_REPO = new LinkedHashMap<>();

    private static DateTimeFormatter formatterMins = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

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

    public enum HealthVaultDataType implements ShimDataType {

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

                        JsonNode dateNode = hvWeight.get("when").get("date");
                        JsonNode timeNode = hvWeight.get("when").get("time");

                        String dateString = dateNode.get("y").asText()
                            + "-" + dateNode.get("m").asText() + "-" + dateNode.get("d").asText();
                        String timeString = timeNode.get("h").asText() + ":" + timeNode.get("m").asText();

                        BodyWeight bodyWeight = new BodyWeightBuilder()
                            .setWeight(hvWeight.get("value").get("display").get("").asText(),
                                MassUnitValue.MassUnit.lb.toString())
                            .setTimeTaken(formatterMins.parseDateTime(dateString + " " + timeString)).build();

                        bodyWeights.add(bodyWeight);
                    }
                    return ShimDataResponse.result(bodyWeights);
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

        Request request = new Request();
        request.setMethodName("GetThings");
        request.setInfo(
            "<info>" +
                "<group max=\"30\">" +
                "<filter>" +
                "<type-id>" + healthVaultDataType.getDataTypeId() + "</type-id>" +
                "<eff-date-min>2014-08-04T00:00:00</eff-date-min>" +
                "<eff-date-max>2014-08-10T23:59:00</eff-date-max>" +
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
                     * and create a new JSON document.
                     */
                    XmlMapper xmlMapper = new XmlMapper();
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(istream);
                    NodeList nodeList = doc.getElementsByTagName("thing");

                    /**
                     * Collect JsonNode from each 'thing' xml dnode.
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
                    for (JsonNode thingNode : thingList) {
                        thingsJson += thingNode.toString() + ",";
                    }
                    thingsJson = thingsJson.substring(0, thingsJson.length() - 1);
                    thingsJson += "]}";

                    /**
                     * Return raw re-built 'things' or a normalized JSON document.
                     */
                    ObjectMapper objectMapper = new ObjectMapper();
                    if (shimDataRequest.getNormalize()) {
                        SimpleModule module = new SimpleModule();
                        module.addDeserializer(ShimDataResponse.class, healthVaultDataType.getNormalizer());
                        objectMapper.registerModule(module);
                        return ShimDataResponse.result(objectMapper.readValue(thingsJson, ShimDataResponse.class));
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
        String callbackUrl = "http://localhost:8080/authorize/"
            + getShimKey() + "/callback?state=" + stateKey;

        authParams.setAuthorizationUrl(getAuthorizationUrl(callbackUrl, ACTION_QS));

        AUTH_PARAMS_REPO.put(stateKey, authParams);
        return authParams;
    }

    @Override
    public AuthorizationResponse handleAuthorizationResponse(
        HttpServletRequest servletRequest) throws ShimException {

        String stateKey = servletRequest.getParameter("state");

        AuthorizationRequestParameters authParams = AUTH_PARAMS_REPO.get(stateKey);
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
