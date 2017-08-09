/*
 * Copyright 2017 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmhealth.shim.ihealth;

/**
 * @author Emerson Farrugia
 */
public enum IHealthScope {

    ACTIVITY_REPORT("OpenApiActivity"),
    BLOOD_GLUCOSE("OpenApiBG"),
    BLOOD_PRESSURE("OpenApiBP"),
    SLEEP_REPORT("OpenApiSleep"),
    BLOOD_OXYGEN("OpenApiSpO2"),
    SPORT_REPORT("OpenApiSport", false),
    WEIGHT("OpenApiWeight");


    final private String apiValue;
    final private boolean availableInSandbox;

    IHealthScope(String apiValue) {

        this(apiValue, true);
    }

    IHealthScope(String apiValue, boolean availableInSandbox) {

        this.apiValue = apiValue;
        this.availableInSandbox = availableInSandbox;
    }

    public String getApiValue() {

        return apiValue;
    }

    public boolean isAvailableInSandbox() {

        return availableInSandbox;
    }
}
