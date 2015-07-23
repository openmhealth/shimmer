package org.openmhealth.shim.withings.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import static java.util.UUID.randomUUID;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Chris Schaefbauer
 */
public abstract class WithingsDataPointMapper<T> implements JsonNodeDataPointMapper<T> {

    public final static String RESOURCE_API_SOURCE_NAME = "Withings Resource API";
    protected final static String TIME_ZONE_PROPERTY = "timezone";
    protected static final String BODY_NODE_PROPERTY = "body";

    /**
     * A convenience method that creates a {@link DataPoint} from a measure and its metadata.
     *
     * @param measure a measure
     * @param externalId the Withings identifier of the measure, if known
     * @param sensed a boolean indicating whether the measure was sensed by a device, if known
     * @param deviceName the name of the Withings device that generated the measure, if known
     * @return the constructed data point
     */
    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, Long externalId, Boolean sensed,
            String deviceName) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        if (sensed != null) {
            provenanceBuilder.setModality(sensed ? SENSED : SELF_REPORTED);
        }

        // additional properties are always subject to change
        DataPointAcquisitionProvenance acquisitionProvenance = provenanceBuilder.build();

        if (deviceName != null) {
            acquisitionProvenance.setAdditionalProperty("device_name", deviceName);
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
}
