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

import org.openmhealth.shim.OAuth2ClientSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


/**
 * @author Emerson Farrugia
 */
@Component
@ConfigurationProperties("openmhealth.shim.fitbit")
public class FitbitClientSettings extends OAuth2ClientSettings {

    private List<String> scopes = Arrays.asList("activity", "heartrate", "sleep", "weight");
    private boolean intradayDataAvailable = false;
    private Integer intradayDataGranularityInMinutes = 1;
    private FitbitPromptType promptType = FitbitPromptType.LOGIN_AND_CONSENT;


    @Override
    public List<String> getScopes() {

        return scopes;
    }

    public void setScopes(List<String> scopes) {

        this.scopes = scopes;
    }

    public boolean isIntradayDataAvailable() {

        return intradayDataAvailable;
    }

    public void setIntradayDataAvailable(boolean intradayDataAvailable) {

        this.intradayDataAvailable = intradayDataAvailable;
    }

    public Integer getIntradayDataGranularityInMinutes() {
        return intradayDataGranularityInMinutes;
    }

    public void setIntradayDataGranularityInMinutes(Integer granularity) {

        // TODO move this into a Constraint
        // TODO revise data type and nullability
        if (granularity != 1 && granularity != 15) {
            throw new IllegalArgumentException("The intraday granularity must be 1 or 15 minutes.");
        }

        this.intradayDataGranularityInMinutes = granularity;
    }

    public FitbitPromptType getPromptType() {

        return promptType;
    }

    public void setPromptType(FitbitPromptType promptType) {

        this.promptType = promptType;
    }
}
