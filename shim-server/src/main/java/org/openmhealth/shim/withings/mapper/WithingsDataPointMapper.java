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
 * Created by Chris Schaefbauer on 6/29/15.
 */
public abstract class WithingsDataPointMapper<T> implements JsonNodeDataPointMapper<T> {

    protected final static String TIME_ZONE_PROPERTY = "timezone";
    public final static String RESOURCE_API_SOURCE_NAME = "Withings Resource API";
    protected static final String BODY_NODE_PROPERTY = "body";

    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, String sourceName, Long externalId,
            Boolean sensed) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(sourceName);

        if (sensed != null) {
            if (sensed) {
                provenanceBuilder.setModality(SENSED);
            }
            else {
                provenanceBuilder.setModality(SELF_REPORTED);
            }

        }

        DataPointAcquisitionProvenance acquisitionProvenance = provenanceBuilder.build();

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
