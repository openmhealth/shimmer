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
import org.openmhealth.shimmer.common.validation.ValidSchemaName;
import org.openmhealth.shimmer.common.validation.ValidSchemaNamespace;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Optional;


/**
 * A bean that represents a search for data points.
 *
 * @author Emerson Farrugia
 */
public class DataPointSearchCriteria {

    private String userId;
    private String schemaNamespace;
    private String schemaName;
    private SchemaVersion schemaVersion;
    private Range<OffsetDateTime> creationTimestampRange;
    private Range<OffsetDateTime> effectiveTimestampRange;
    private String acquisitionSourceId; // TODO confirm if we want to run with this name


    /**
     * @return the user the data points belong to
     */
    @NotNull
    @Size(min = 1)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the schema namespace of the body of the data points
     */
    @NotNull
    @ValidSchemaNamespace
    public String getSchemaNamespace() {
        return schemaNamespace;
    }

    public void setSchemaNamespace(String schemaNamespace) {
        this.schemaNamespace = schemaNamespace;
    }

    /**
     * @return the schema name of the body of the data points
     */
    @NotNull
    @ValidSchemaName
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * @return the schema version of the body of the data points
     */
    public Optional<SchemaVersion> getSchemaVersion() {
        return Optional.ofNullable(schemaVersion);
    }

    public void setSchemaVersion(SchemaVersion schemaVersion) {
        this.schemaVersion = schemaVersion;
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

    @Size(min = 1)
    public Optional<String> getAcquisitionSourceId() {
        return Optional.ofNullable(acquisitionSourceId);
    }

    public void setAcquisitionSourceId(String acquisitionSourceId) {
        this.acquisitionSourceId = acquisitionSourceId;
    }
}
