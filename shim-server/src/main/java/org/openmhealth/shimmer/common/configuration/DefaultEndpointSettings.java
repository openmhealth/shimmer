/*
 * Copyright 2015 Open mHealth
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
 */

package org.openmhealth.shimmer.common.configuration;

import org.springframework.web.util.UriTemplate;

import java.util.Optional;


/**
 * @author Emerson Farrugia
 */
public class DefaultEndpointSettings implements EndpointSettings {

    private String id;
    private UriTemplate uriTemplate;
    private DateTimeQuerySettings effectiveDateTimeQuerySettings;
    private DateTimeQuerySettings creationDateTimeQuerySettings;
    private DateTimeQuerySettings modificationDateTimeQuerySettings;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(UriTemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    @Override
    public Optional<DateTimeQuerySettings> getEffectiveDateTimeQuerySettings() {
        return Optional.ofNullable(effectiveDateTimeQuerySettings);
    }

    public void setEffectiveDateTimeQuerySettings(DateTimeQuerySettings settings) {
        this.effectiveDateTimeQuerySettings = settings;
    }

    @Override
    public Optional<DateTimeQuerySettings> getCreationDateTimeQuerySettings() {
        return Optional.ofNullable(creationDateTimeQuerySettings);
    }

    public void setCreationDateTimeQuerySettings(DateTimeQuerySettings settings) {
        this.creationDateTimeQuerySettings = settings;
    }

    @Override
    public Optional<DateTimeQuerySettings> getModificationDateTimeQuerySettings() {
        return Optional.ofNullable(modificationDateTimeQuerySettings);
    }

    public void setModificationDateTimeQuerySettings(DateTimeQuerySettings settings) {
        this.modificationDateTimeQuerySettings = settings;
    }
}
