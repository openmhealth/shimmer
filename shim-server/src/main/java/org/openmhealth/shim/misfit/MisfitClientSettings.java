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

package org.openmhealth.shim.misfit;

import org.openmhealth.shim.OAuth2ClientSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


/**
 * @author Emerson Farrugia
 */
@Component
@ConfigurationProperties("openmhealth.shim.misfit")
public class MisfitClientSettings extends OAuth2ClientSettings {

    private List<String> scopes = Arrays.asList(
            "public",
            "birthday",
            "email"
    );

    /**
     * Scopes aren't actually supported by Misfit.
     *
     * @see <a href="https://build.misfit.com/docs/cloudapi/api_references#authorize-a-3rd-party-app">Misfit scope
     * documentation</a>
     */
    @Override
    public List<String> getScopes() {

        return scopes;
    }

    public void setScopes(List<String> scopes) {

        this.scopes = scopes;
    }
}
