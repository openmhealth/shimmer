package org.openmhealth.shim.fitbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.openmhealth.shim.*;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class FitbitShim extends OAuth1ShimBase {
    public static final String SHIM_KEY = "fitbit";

    private static final String DATA_URL = "https://api.fitbit.com ";

    private static final String REQUEST_TOKEN_URL = "https://www.fitbit.com/oauth/request_token";

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth/authorize";

    private static final String TOKEN_URL = "https://www.fitbit.com/oauth/access_token";

    public static final String FITBIT_CLIENT_ID = "7da3c2e5e74d4492ab6bb3286fc32c6b";

    public static final String FITBIT_CLIENT_SECRET = "455a383f80de45d6a4f9b09e841da1f4";

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
        return FITBIT_CLIENT_ID;
    }

    @Override
    public String getClientId() {
        return FITBIT_CLIENT_SECRET;
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

    protected HttpMethod getRequestTokenMethod() {
        return HttpMethod.POST;
    }

    protected HttpMethod getAccessTokenMethod() {
        return HttpMethod.POST;
    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {
        AccessParameters accessParameters = shimDataRequest.getAccessParameters();
        String accessToken = accessParameters.getAccessToken();
        String tokenSecret = accessParameters.getTokenSecret();

        String endPointUrl = DATA_URL;

        endPointUrl += "/1/user/-/activities/date/2014-07-13.json";

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
        }
    }
}
