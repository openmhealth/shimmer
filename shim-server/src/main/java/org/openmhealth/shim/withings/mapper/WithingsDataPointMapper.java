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

package org.openmhealth.shim.withings.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static java.util.UUID.randomUUID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Chris Schaefbauer
 */
public abstract class WithingsDataPointMapper<T extends Measure> implements JsonNodeDataPointMapper<T> {

    public final static String RESOURCE_API_SOURCE_NAME = "Withings Resource API";
    protected static final String BODY_NODE_PROPERTY = "body";

    /**
     * A convenience method that creates a {@link DataPoint} from a measure and its metadata.
     *
     * @param measure a measure
     * @param externalId the Withings identifier of the measure, if known
     * @param sensed a boolean indicating whether the measure was sensed by a device, if known
     * @param device the Withings device that generated the measure, if known
     * @return the constructed data point
     */
    protected DataPoint<T> newDataPoint(T measure, String externalId, Boolean sensed,
            WithingsDevice device) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        if (sensed != null) {
            provenanceBuilder.setModality(sensed ? SENSED : SELF_REPORTED);
        }

        // additional properties are always subject to change
        DataPointAcquisitionProvenance acquisitionProvenance = provenanceBuilder.build();

        if (device != null) {
            acquisitionProvenance.setAdditionalProperty("device_name", device.getDisplayName());
        }

        if (externalId != null) {
            // TODO discuss the name of the external identifier, to make it clear it's the ID used by the source
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }

        DataPointHeader header = new DataPointHeader.Builder(randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

        return new DataPoint<>(header, measure);
    }

    protected OffsetDateTime asOffsetDateTime(long epochSeconds, String timeZoneId) {

        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.of(timeZoneId)).toOffsetDateTime();
    }
}
