package org.openmhealth.shim.withings;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.openmhealth.shim.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class WithingsShim extends OAuth1ShimBase {

    public static final String SHIM_KEY = "withings";

    private static final String DATA_URL = "http://wbsapi.withings.net";

    private static final String REQUEST_TOKEN_URL = "https://oauth.withings.com/account/request_token";

    private static final String AUTHORIZE_URL = "https://oauth.withings.com/account/authorize";

    private static final String TOKEN_URL = "https://oauth.withings.com/account/access_token";

    public static final String WITHINGS_CLIENT_ID = "bfb8c6b3bffd8b83b39e67dfe40f81c8289b8d0bbfb97b27953925d6f3bc";

    public static final String WITHINGS_CLIENT_SECRET = "d9182878bc9999158cd748fc2fe12d81ffcce9c9f77093972e93c0f";

    public WithingsShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo,
                        ShimServerConfig shimServerConfig) {
        super(authorizationRequestParametersRepo, shimServerConfig);
    }

    @Override
    public List<String> getScopes() {
        return null; //noop!
    }

    @Override
    public String getShimKey() {
        return SHIM_KEY;
    }

    @Override
    public String getClientSecret() {
        return WITHINGS_CLIENT_SECRET;
    }

    @Override
    public String getClientId() {
        return WITHINGS_CLIENT_ID;
    }

    @Override
    public String getBaseRequestTokenUrl() {
        return REQUEST_TOKEN_URL;
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
    protected void loadAdditionalAccessParameters(HttpServletRequest request, AccessParameters accessParameters) {
        Map<String, Object> addlParams =
            accessParameters.getAdditionalParameters();
        addlParams = addlParams != null ? addlParams : new LinkedHashMap<String, Object>();
        addlParams.put("userid", request.getParameter("userid"));
    }

    public enum WithingsDataType implements ShimDataType {

        BODY("getmeas", new JsonDeserializer<ShimDataResponse>() {
            @Override
            public ShimDataResponse deserialize(JsonParser jsonParser,
                                                DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
                return null;
            }
        });

        private String endPointMethod;

        private JsonDeserializer<ShimDataResponse> normalizer;

        WithingsDataType(String endPointUrl,
                         JsonDeserializer<ShimDataResponse> normalizer) {
            this.endPointMethod = endPointUrl;
            this.normalizer = normalizer;
        }

        @Override
        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return normalizer;
        }

        public String getEndPointMethod() {
            return endPointMethod;
        }
    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {
        AccessParameters accessParameters = shimDataRequest.getAccessParameters();
        String accessToken = accessParameters.getAccessToken();
        String tokenSecret = accessParameters.getTokenSecret();
        final String userid = accessParameters.getAdditionalParameters().get("userid").toString();

        String endPointUrl = DATA_URL;

        long startTime = new Date().getTime() / 1000;

        endPointUrl += "/measure?action=getmeas";
        //"&meastype=4";
        endPointUrl += "&userid=" + userid;
        //endPointUrl += "&startdate=" + (startTime - 3600);
        //endPointUrl += "&enddate=" + (startTime - 3 * 24 * 3600);

        URL url = signUrl(endPointUrl, accessToken, tokenSecret, null);

        // Fetch and decode the JSON data.
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonData;

        HttpGet get = new HttpGet(url.toString());
        HttpResponse response;
        try {
            response = httpClient.execute(get);
            HttpEntity responseEntity = response.getEntity();
            jsonData = objectMapper.readTree(responseEntity.getContent());
            return ShimDataResponse.result(jsonData);

        } catch (IOException e) {
            throw new ShimException("Could not fetch data", e);
        } finally {
            get.releaseConnection();
        }
    }
}
