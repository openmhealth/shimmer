package org.openmhealth.shim.healthvault;

import com.microsoft.hsg.*;
import org.openmhealth.shim.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
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
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {
        final List<WeightInfo> weightList = new ArrayList<>();

        Request request = new Request();
        request.setMethodName("GetThings");
        request.setInfo("<info><group max=\"30\"><filter><type-id>" +
            WeightInfo.WeightType +
            "</type-id></filter><format><section>core</section><xml/></format></group></info>");

        RequestTemplate template = new RequestTemplate(connection);
        List<WeightInfo> weightInfoList = template.makeRequest(shimDataRequest.getAccessParameters(),
            request, new Marshaller<List<WeightInfo>>() {
                public List<WeightInfo> marshal(InputStream istream) throws Exception {
                    InputSource isource = new InputSource(istream);
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String exp = "//thing";
                    NodeList things = (NodeList) xpath.evaluate(exp,
                        isource,
                        XPathConstants.NODESET);

                    int count = Math.min(50, things.getLength());
                    for (int i = 0; i < count; i++) {
                        Node thing = things.item(i);
                        String weight = xpath.evaluate("data-xml/weight/value/display", thing);
                        String id = xpath.evaluate("thing-id", thing);
                        weightList.add(new WeightInfo(id, weight));
                    }
                    return weightList;
                }
            });

        return ShimDataResponse.result(weightInfoList);
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
