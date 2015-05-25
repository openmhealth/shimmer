package org.openmhealth.shim.misfit.mapper;

import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DataPointHeader;
import org.openmhealth.schema.domain.omh.Measure;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import java.util.UUID;

import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;


/**
 * @author Emerson Farrugia
 */
public abstract class MisfitDataPointMapper<T> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Misfit Resource API";


    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, String sourceName, String externalId,
            Boolean sensed) {

        DataPointAcquisitionProvenance.Builder provenanceBuilder =
                new DataPointAcquisitionProvenance.Builder(sourceName);

        if (sensed != null && sensed) {
            provenanceBuilder.setModality(SENSED);
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
