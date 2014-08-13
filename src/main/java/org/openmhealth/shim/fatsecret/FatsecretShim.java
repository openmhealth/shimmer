package org.openmhealth.shim.fatsecret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.openmhealth.shim.*;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Encapsulates parameters specific to fatsecret api.
 */
public class FatsecretShim extends OAuth1ShimBase {

    public static final String SHIM_KEY = "fatsecret";

    private static final String DATA_URL = "http://platform.fatsecret.com/rest/server.api";

    private static final String REQUEST_TOKEN_URL = "http://www.fatsecret.com/oauth/request_token";

    private static final String AUTHORIZE_URL = "http://www.fatsecret.com/oauth/authorize";

    private static final String TOKEN_URL = "http://www.fatsecret.com/oauth/access_token";

    public static final String FATSECRET_CLIENT_ID = "d1c59d7f9c8243f0b2eaef9ea43278a0";

    public static final String FATSECRET_CLIENT_SECRET = "c16dd2eeea804a7cba1180293d4b770c";

    public FatsecretShim(AuthorizationRequestParametersRepo authorizationRequestParametersRepo) {
        super(authorizationRequestParametersRepo);
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
        return FATSECRET_CLIENT_SECRET;
    }

    @Override
    public String getClientId() {
        return FATSECRET_CLIENT_ID;
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
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {

        long numToSkip = 0;
        long numToReturn = 3;

        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.AUGUST, 1);
        Date endDate = new Date(cal.getTimeInMillis());
        cal.add(Calendar.DATE, -1);
        Date startDate = new Date(cal.getTimeInMillis());

        DateTime startTime = new DateTime(startDate.getTime());
        DateTime endTime = new DateTime(endDate.getTime());

        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);

        int days = 16283; //Days.daysBetween(epoch, new DateTime()).getDays() - 1;

        String endPoint = "food_entries.get";

        String accessToken = shimDataRequest.getAccessParameters().getAccessToken();
        String tokenSecret = shimDataRequest.getAccessParameters().getTokenSecret();

        URL url = signUrl(DATA_URL + "?date=" + days + "&format=json&method=" + endPoint,
            accessToken, tokenSecret, null);
        System.out.println("Signed URL is: \n\n" + url);

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
