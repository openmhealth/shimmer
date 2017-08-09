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

package org.openmhealth.shim.fitbit;


/**
 * @author Emerson Farrugia
 * @see <a href="https://dev.fitbit.com/docs/oauth2/#authorization-page">Fitbit authorization page documentation</a>
 */
public enum FitbitPromptType {

    // no login if logged in, no consent if previously consented
    NONE("none"),

    // no login if logged in, consent even if previously consented
    CONSENT("consent"),

    // login even if previously logged in, no consent if previously consented
    LOGIN("login"),

    // login and consent, even if previously logged in and consented
    LOGIN_AND_CONSENT("login consent");

    private String queryParameterValue;


    FitbitPromptType(String queryParameterValue) {

        this.queryParameterValue = queryParameterValue;
    }

    public String getQueryParameterValue() {

        return queryParameterValue;
    }
}
