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

package org.openmhealth.shimmer.common.domain;

import com.google.common.collect.Range;
import org.openmhealth.schema.domain.omh.SchemaVersion;
import org.openmhealth.shimmer.common.configuration.EndpointConfigurationProperties;
import org.openmhealth.shimmer.common.domain.pagination.PaginationStatus;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;


/**
 * TODO: Identify all the properties this object needs
 *
 * @author Emerson Farrugia
 */
public class DataPointRequest {


    private String userId;
    private String schemaNamespace;
    private String schemaName;
    private SchemaVersion schemaVersion;
    private Range<OffsetDateTime> creationTimestampRange;
    private Range<OffsetDateTime> effectiveTimestampRange;
    private String api; // TODO pick a better name, what if it's proxied, "api"?

    private EndpointConfigurationProperties configurationPropertiesForTargetEndpoint;
    private PaginationStatus paginationStatus;

    public DataPointRequest(EndpointConfigurationProperties endpoint, String userId, String schemaNamespace,
            String schemaName, String schemaVersion) {

        checkNotNull(userId);
        checkArgument(!isNullOrEmpty(userId));

        checkNotNull(schemaNamespace);
        checkNotNull(schemaName);
        // TODO determine how restrictive the search criteria should be
        checkNotNull(schemaVersion);

        checkNotNull(endpoint);

        this.userId = userId;
        this.configurationPropertiesForTargetEndpoint = endpoint;

        this.schemaNamespace = schemaNamespace;
        this.schemaName = schemaName;
        this.schemaVersion = new SchemaVersion(schemaVersion);

    }

    public String getUserId() {
        return userId;
    }

    public Optional<Range<OffsetDateTime>> getCreationTimestampRange() {
        return Optional.ofNullable(creationTimestampRange);
    }

    public void setCreationTimestampRange(Range<OffsetDateTime> creationTimestampRange) {
        this.creationTimestampRange = creationTimestampRange;
    }

    public Optional<Range<OffsetDateTime>> getEffectiveTimestampRange() {
        return Optional.ofNullable(effectiveTimestampRange);
    }

    public void setEffectiveTimestampRange(Range<OffsetDateTime> effectiveTimestampRange) {
        this.effectiveTimestampRange = effectiveTimestampRange;
    }

    public Optional<String> getApi() {
        return Optional.ofNullable(api);
    }

    public void setApi(String api) {
        this.api = api;
    }

    public EndpointConfigurationProperties getEndpoint() {
        return configurationPropertiesForTargetEndpoint;
    }

    public void setEndpoint(EndpointConfigurationProperties configurationPropertiesForTargetEndpoint) {
        this.configurationPropertiesForTargetEndpoint = configurationPropertiesForTargetEndpoint;
    }

    public String getSchemaNamespace() {
        return schemaNamespace;
    }

    public void setSchemaNamespace(String schemaNamespace) {
        this.schemaNamespace = schemaNamespace;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public SchemaVersion getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(SchemaVersion schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Optional<PaginationStatus> getPaginationStatus() {
        return Optional.ofNullable(paginationStatus);
    }

    public void setPaginationStatus(PaginationStatus paginationStatus) {
        this.paginationStatus = paginationStatus;
    }
}
