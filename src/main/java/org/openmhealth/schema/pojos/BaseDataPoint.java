package org.openmhealth.schema.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Common code for data points, primarily related
 * to metadata.
 */
public abstract class BaseDataPoint implements DataPoint {

    @JsonProperty(value = "metadata")
    protected Metadata metadata;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
