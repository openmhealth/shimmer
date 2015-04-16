package org.openmhealth.shim.testing;


import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.openmhealth.schema.pojos.DataPoint;
import org.openmhealth.schema.pojos.HeartRateUnitValue;
import org.openmhealth.schema.pojos.SleepDurationUnitValue;
import org.openmhealth.schema.pojos.generic.DurationUnitValue.DurationUnit;
import org.openmhealth.schema.pojos.generic.LengthUnitValue;
import org.openmhealth.schema.pojos.generic.MassUnitValue;
import org.openmhealth.schema.pojos.generic.TimeFrame;
import org.openmhealth.shim.ShimDataResponse;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.io.Resources;

public abstract class ShimTestSupport {

    @SuppressWarnings("unchecked")
    protected static <T extends DataPoint> List<T> read(String resource, String schema, JsonDeserializer<ShimDataResponse> deserializer) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(ShimDataResponse.class, deserializer);
            objectMapper.registerModule(module);
            URL url = Resources.getResource(resource);
            ShimDataResponse response = objectMapper.readValue(url.openStream(), ShimDataResponse.class);
            Map<String, List<T>> map = (Map<String, List<T>>) response.getBody();
            return map != null ? map.get(schema) : null;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    protected static void assertTimeFrameEquals(String expectedTime, TimeFrame actual) {
        assertEquals(TimeFrame.withDateTime(DateTime.parse(expectedTime)), actual);
    }

    protected static void assertTimeFrameEquals(String expectedStart, String expectedEnd, TimeFrame actual) {
        assertEquals(TimeFrame.withTimeInterval(DateTime.parse(expectedStart), DateTime.parse(expectedEnd)), actual);
    }

    protected static void assertTimeFrameEquals(String expectedStart, int expectedDurationValue, DurationUnit expectedDurationUnit, TimeFrame actual) {
        assertEquals(TimeFrame.withTimeInterval(DateTime.parse(expectedStart), Double.valueOf(expectedDurationValue), expectedDurationUnit), actual);
    }

    protected static void assertLengthUnitEquals(String expectedValue, LengthUnitValue.LengthUnit expectedUnit, LengthUnitValue actual) {
        assertEquals(new BigDecimal(expectedValue), actual.getValue());
        assertEquals(expectedUnit, actual.getUnit());
    }

    protected static void assertMassUnitEquals(String expectedValue, MassUnitValue.MassUnit expectedUnit, MassUnitValue actual) {
        assertEquals(new BigDecimal(expectedValue), actual.getValue());
        assertEquals(expectedUnit, actual.getUnit());
    }

    protected static void assertHeartRateUnitEquals(int expectedValue, HeartRateUnitValue.Unit expectedUnit, HeartRateUnitValue actual) {
        assertEquals(Integer.valueOf(expectedValue), actual.getValue());
        assertEquals(expectedUnit, actual.getUnit());
    }

    protected static void assertSleepDurationUnitEquals(int expectedValue, SleepDurationUnitValue.Unit expectedUnit, SleepDurationUnitValue actual) {
        assertEquals(expectedValue, actual.getValue().intValue());
        assertEquals(expectedUnit, actual.getUnit());
    }
}
