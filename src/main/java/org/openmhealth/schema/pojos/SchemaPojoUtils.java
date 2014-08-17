package org.openmhealth.schema.pojos;

import java.util.HashMap;
import java.util.Map;

public class SchemaPojoUtils {

    //todo: any way to do this via reflection?
    private final static Map<String, Class<? extends DataPoint>> schemaToNormalizedTypeMap =
        new HashMap<String, Class<? extends DataPoint>>() {{
            put(Activity.SCHEMA_ACTIVITY, Activity.class);
            put(NumberOfSteps.SCHEMA_NUMBER_OF_STEPS, NumberOfSteps.class);
            put(BodyWeight.SCHEMA_BODY_WEIGHT, BodyWeight.class);
            put(BodyHeight.SCHEMA_BODY_HEIGHT, BodyHeight.class);
            put(BloodPressure.SCHEMA_BLOOD_PRESSURE, BloodPressure.class);
            put(BloodGlucose.SCHEMA_BLOOD_GLUCOSE, BloodGlucose.class);
            put(SleepDuration.SCHEMA_SLEEP_DURATION, SleepDuration.class);
            put(HeartRate.SCHEMA_HEART_RATE, HeartRate.class);
        }};

    public static Class<? extends DataPoint> getSchemaClass(String schemaName) {
        String clean = schemaName.trim().toLowerCase();
        if (schemaToNormalizedTypeMap.containsKey(clean)) {
            return schemaToNormalizedTypeMap.get(clean);
        }
        return null;
    }

    public static String getSchemaName(Class<? extends DataPoint> givenClazz) {
        for (String schemaName : schemaToNormalizedTypeMap.keySet()) {
            Class<? extends DataPoint> clazz = schemaToNormalizedTypeMap.get(schemaName);
            if (clazz.equals(givenClazz)) {
                return schemaName;
            }
        }
        return null;
    }
}
