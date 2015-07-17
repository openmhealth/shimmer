package org.openmhealth.shim.withings.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.util.UUID;

import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Chris Schaefbauer
 */
public abstract class WithingsDataPointMapper<T> implements JsonNodeDataPointMapper<T> {

    protected final static String TIME_ZONE_PROPERTY = "timezone";
    public final static String RESOURCE_API_SOURCE_NAME = "Withings Resource API";
    protected static final String BODY_NODE_PROPERTY = "body";

    /**
     * Creates a {@link DataPoint} from a measure, and its meta-information, generated from a Withings measure specific
     * {@link WithingsDataPointMapper}
     *
     * @param <T> the {@link Measure} type
     * @param measure {@link Measure} of type T, generated through a measure specific data point mapper
     * @param externalId the external id from the Withings API Response, can be null if the corresponding property is
     * missing from the Withings response
     * @param sensed a boolean indicating whether the datapoint was sensed by a device for now, can be null if the
     * corresponding property is missing from the Withings response
     * @param deviceName the name of the Withings device that generated the data point, can be null if the corresponding
     * property is missing from the Withings response
     * @return a datapoint
     */
    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, Long externalId,
            Boolean sensed, String deviceName) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME);

        if (sensed != null) {
            if (sensed) {
                provenanceBuilder.setModality(SENSED);
            }
            else {
                provenanceBuilder.setModality(SELF_REPORTED);
            }
        }

        DataPointAcquisitionProvenance acquisitionProvenance = provenanceBuilder.build();
        if (deviceName != null) {
            acquisitionProvenance.setAdditionalProperty("device_name", deviceName);
        }

        // TODO discuss the name of the external identifier, to make it clear it's the ID used by the source
        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }

        DataPointHeader header = new DataPointHeader.Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance)
                .build();

        return new DataPoint<>(header, measure);
    }

}
