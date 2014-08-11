package org.openmhealth.shim;

import com.fasterxml.jackson.databind.JsonDeserializer;

public interface ShimDataType {

    JsonDeserializer<ShimDataResponse> getNormalizer();
}
