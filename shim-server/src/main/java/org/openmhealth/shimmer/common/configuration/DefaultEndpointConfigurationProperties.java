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
public class DefaultEndpointConfigurationProperties implements EndpointConfigurationProperties {

    private String id;
    private UriTemplate uriTemplate;
    private DateTimeQueryConfigurationProperties effectiveDateTimeQuerySettings;
    private DateTimeQueryConfigurationProperties creationDateTimeQuerySettings;
    private DateTimeQueryConfigurationProperties modificationDateTimeQuerySettings;
    private String apiSourceName;
    private PaginationSettings paginationSettings;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getApiSourceName() {
        return apiSourceName;
    }

    public void setApiSourceName(String apiSourceName) {
        this.apiSourceName = apiSourceName;
    }

    @Override
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(UriTemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    @Override
    public Optional<DateTimeQueryConfigurationProperties> getEffectiveDateTimeQuerySettings() {
        return Optional.ofNullable(effectiveDateTimeQuerySettings);
    }

    public void setEffectiveDateTimeQuerySettings(DateTimeQueryConfigurationProperties settings) {
        this.effectiveDateTimeQuerySettings = settings;
    }

    @Override
    public Optional<DateTimeQueryConfigurationProperties> getCreationDateTimeQuerySettings() {
        return Optional.ofNullable(creationDateTimeQuerySettings);
    }

    public void setCreationDateTimeQuerySettings(DateTimeQueryConfigurationProperties settings) {
        this.creationDateTimeQuerySettings = settings;
    }

    @Override
    public Optional<DateTimeQueryConfigurationProperties> getModificationDateTimeQuerySettings() {
        return Optional.ofNullable(modificationDateTimeQuerySettings);
    }

    public void setModificationDateTimeQuerySettings(DateTimeQueryConfigurationProperties settings) {
        this.modificationDateTimeQuerySettings = settings;
    }

    @Override
    public Optional<PaginationSettings> getPaginationSettings() {
        return Optional.ofNullable(this.paginationSettings);
    }

    public void setPaginationSettings(PaginationSettings settings){
        this.paginationSettings = settings;
    }

}
