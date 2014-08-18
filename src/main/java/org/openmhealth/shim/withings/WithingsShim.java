package org.openmhealth.shim.withings;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openmhealth.schema.pojos.*;
import org.openmhealth.schema.pojos.build.BodyHeightBuilder;
import org.openmhealth.schema.pojos.build.BodyWeightBuilder;
import org.openmhealth.schema.pojos.build.HeartRateBuilder;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;
import org.openmhealth.shim.*;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
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
    public ShimDataType[] getShimDataTypes() {
        return new ShimDataType[]{
            WithingsDataType.BODY
        };
    }

    @Override
    protected void loadAdditionalAccessParameters(HttpServletRequest request, AccessParameters accessParameters) {
        Map<String, Object> addlParams =
            accessParameters.getAdditionalParameters();
        addlParams = addlParams != null ? addlParams : new LinkedHashMap<String, Object>();
        addlParams.put("userid", request.getParameter("userid"));
    }

    public enum MeasureType {

        WEIGHT(1), HEIGHT(4), FAT_FREE_MASS(5), FAT_RATIO(6),
        FAT_MASS_WEIGHT(8),
        BLOOD_PRESSURE_DIASTOLIC(9),
        BLOOD_PRESSURE_SYSTOLIC(10),
        HEART_PULSE(11),
        SP02(54);

        private int intVal;

        MeasureType(int measureType) {
            this.intVal = measureType;
        }

        public int getIntVal() {
            return intVal;
        }

        public static MeasureType valueFor(int intVal) {
            for (MeasureType typeEnum : MeasureType.values()) {
                if (typeEnum.getIntVal() == intVal) {
                    return typeEnum;
                }
            }
            return null;
        }
    }

    public enum WithingsDataType implements ShimDataType {

        BODY("measure?action=getmeas", "startdate", "enddate", true, true,
            new JsonDeserializer<ShimDataResponse>() {

                @Override
                @SuppressWarnings("unchecked")
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException {

                    JsonNode responseNode = jsonParser.getCodec().readTree(jsonParser);
                    String rawJson = responseNode.toString();

                    List<BodyWeight> bodyWeights = new ArrayList<>();
                    List<BodyHeight> bodyHeights = new ArrayList<>();
                    List<BloodPressure> bloodPressures = new ArrayList<>();
                    List<HeartRate> heartRates = new ArrayList<>();

                    JsonPath measuresPath = JsonPath.compile("$.body.measuregrps[*]");
                    List<Object> wMeasureGroups = JsonPath.read(rawJson, measuresPath.getPath());
                    if (CollectionUtils.isEmpty(wMeasureGroups)) {
                        return ShimDataResponse.result(null);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    for (Object wMeasureGroup : wMeasureGroups) {
                        JsonNode wMeasure = mapper.readTree(((JSONObject) wMeasureGroup).toJSONString());
                        DateTime dateTime = new DateTime(Long.parseLong(wMeasure.get("date").asText()) * 1000);
                        ArrayNode measureNodes = (ArrayNode) wMeasure.get("measures");
                        SystolicBloodPressure systolic = null;
                        DiastolicBloodPressure diastolic = null;
                        for (JsonNode measureNode : measureNodes) {
                            MeasureType measureType = MeasureType.valueFor(measureNode.get("type").asInt());
                            Double valueAsDouble = measureNode.get("value").asDouble();
                            Double multiplier = Math.pow(10, measureNode.get("unit").asDouble());
                            BigDecimal measureValue = new BigDecimal(valueAsDouble * multiplier);
                            switch (measureType) {
                                case WEIGHT:
                                    bodyWeights.add(new BodyWeightBuilder()
                                        .setTimeTaken(dateTime)
                                        .setWeight(measureValue + "",
                                            MassUnitValue.MassUnit.kg.toString())
                                        .build());
                                    break;
                                case HEIGHT:
                                    bodyHeights.add(new BodyHeightBuilder()
                                        .setTimeTaken(dateTime)
                                        .setHeight(
                                            measureValue + "",
                                            LengthUnitValue.LengthUnit.m.toString())
                                        .build());
                                    break;
                                case BLOOD_PRESSURE_DIASTOLIC:
                                    diastolic = new DiastolicBloodPressure(
                                        measureValue, BloodPressureUnit.mmHg);
                                    break;
                                case BLOOD_PRESSURE_SYSTOLIC:
                                    systolic = new SystolicBloodPressure(
                                        measureValue, BloodPressureUnit.mmHg);
                                    break;
                                case HEART_PULSE:
                                    heartRates.add(new HeartRateBuilder()
                                        .setTimeTaken(dateTime)
                                        .setRate(measureValue.intValue() + "")
                                        .build());
                                    break;
                            }
                        }
                        if(systolic != null && diastolic != null){
                            BloodPressure bloodPressure = new BloodPressure();
                            bloodPressure.setSystolic(systolic);
                            bloodPressure.setDiastolic(diastolic);
                            bloodPressure.setEffectiveTimeFrame(new TimeFrame(dateTime, null));
                            bloodPressures.add(bloodPressure);
                        }
                    }
                    Map<String, Object> results = new HashMap<>();
                    results.put(BodyWeight.SCHEMA_BODY_WEIGHT, bodyWeights);
                    results.put(BodyHeight.SCHEMA_BODY_HEIGHT, bodyHeights);
                    results.put(BloodPressure.SCHEMA_BLOOD_PRESSURE, bloodPressures);
                    results.put(HeartRate.SCHEMA_HEART_RATE, heartRates);
                    return ShimDataResponse.result(results);
                }
            }),

        ACTIVITY("v2/measure?action=getactivity", "startdateymd", "enddateymd", false, false,
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                    return null;
                }
            }),

        INTRADAY("v2/measure?action=getintradayactivity", "startdate", "enddate", false, true,
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                    return null;
                }
            }),

        SLEEP("v2/sleep?action=get", "startdate", "enddate", false, true,
            new JsonDeserializer<ShimDataResponse>() {
                @Override
                public ShimDataResponse deserialize(JsonParser jsonParser,
                                                    DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                    return null;
                }
            });

        private String endPointMethod;

        private String dateParamStart;

        private String dateParamEnd;

        private boolean isNumToReturnSupported;

        private boolean isTimeStampFormat;

        private JsonDeserializer<ShimDataResponse> normalizer;

        WithingsDataType(String endPointUrl,
                         String dateParamStart,
                         String dateParamEnd,
                         boolean isNumToReturnSupported,
                         boolean isTimeStampFormat,
                         JsonDeserializer<ShimDataResponse> normalizer) {
            this.endPointMethod = endPointUrl;
            this.normalizer = normalizer;
            this.dateParamStart = dateParamStart;
            this.dateParamEnd = dateParamEnd;
            this.isNumToReturnSupported = isNumToReturnSupported;
            this.isTimeStampFormat = isTimeStampFormat;
        }

        @Override
        public JsonDeserializer<ShimDataResponse> getNormalizer() {
            return normalizer;
        }

        public String getEndPointMethod() {
            return endPointMethod;
        }

        public String getDateParamStart() {
            return dateParamStart;
        }

        public String getDateParamEnd() {
            return dateParamEnd;
        }

        public boolean isNumToReturnSupported() {
            return isNumToReturnSupported;
        }

        public boolean isTimeStampFormat() {
            return isTimeStampFormat;
        }
    }

    @Override
    public ShimDataResponse getData(ShimDataRequest shimDataRequest) throws ShimException {
        AccessParameters accessParameters = shimDataRequest.getAccessParameters();
        String accessToken = accessParameters.getAccessToken();
        String tokenSecret = accessParameters.getTokenSecret();
        final String userid = accessParameters.getAdditionalParameters().get("userid").toString();

        String endPointUrl = DATA_URL;

        final WithingsDataType withingsDataType;
        try {
            withingsDataType = WithingsDataType.valueOf(
                shimDataRequest.getDataTypeKey().trim().toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new ShimException("Null or Invalid data type parameter: "
                + shimDataRequest.getDataTypeKey()
                + " in shimDataRequest, cannot retrieve data.");
        }

        final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        /***
         * Setup default date parameters
         */
        DateTime today = new DateTime();

        DateTime startDate = shimDataRequest.getStartDate() == null ?
            today.minusDays(1) : shimDataRequest.getStartDate();
        String dateStart = startDate.toString(formatter);
        long dateStartTs = startDate.toDate().getTime() / 1000;

        DateTime endDate = shimDataRequest.getEndDate() == null ?
            today.plusDays(1) : shimDataRequest.getEndDate();
        String dateEnd = endDate.toString(formatter);
        long dateEndTs = endDate.toDate().getTime() / 1000;

        endPointUrl += "/" + withingsDataType.getEndPointMethod();

        //"&meastype=4";

        endPointUrl += "&userid=" + userid;

        if (withingsDataType.isTimeStampFormat()) {
            endPointUrl += "&" + withingsDataType.getDateParamStart() + "=" + dateStartTs;
            endPointUrl += "&" + withingsDataType.getDateParamEnd() + "=" + dateEndTs;
        } else {
            endPointUrl += "&" + withingsDataType.getDateParamStart() + "=" + dateStart;
            endPointUrl += "&" + withingsDataType.getDateParamEnd() + "=" + dateEnd;
        }

        if (withingsDataType.isNumToReturnSupported()
            && shimDataRequest.getNumToReturn() != null) {
            endPointUrl += "&limit=" + shimDataRequest.getNumToReturn();
        }

        URL url = signUrl(endPointUrl, accessToken, tokenSecret, null);

        // Fetch and decode the JSON data.
        ObjectMapper objectMapper = new ObjectMapper();
        HttpGet get = new HttpGet(url.toString());
        HttpResponse response;
        try {
            response = httpClient.execute(get);
            HttpEntity responseEntity = response.getEntity();

            if (shimDataRequest.getNormalize()) {
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ShimDataResponse.class, withingsDataType.getNormalizer());
                objectMapper.registerModule(module);
                return objectMapper.readValue(responseEntity.getContent(), ShimDataResponse.class);
            } else {
                return ShimDataResponse.result(objectMapper.readTree(responseEntity.getContent()));
            }
        } catch (IOException e) {
            throw new ShimException("Could not fetch data", e);
        } finally {
            get.releaseConnection();
        }
    }
}
