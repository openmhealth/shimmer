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

package org.openmhealth.schema.pojos;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Danilo Bonilla
 */
public class SchemaPojoUtils {

    //todo: find better way with reflection?
    private final static Map<String, Class<? extends DataPoint>> schemaToNormalizedTypeMap =
        new HashMap<String, Class<? extends DataPoint>>() {{
            put(Activity.SCHEMA_ACTIVITY, Activity.class);
            put(StepCount.SCHEMA_STEP_COUNT, StepCount.class);
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
