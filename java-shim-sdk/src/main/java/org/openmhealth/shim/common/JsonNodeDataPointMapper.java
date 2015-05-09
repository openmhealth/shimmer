package org.openmhealth.shim.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;


/**
 * @author Emerson Farrugia
 */
public interface JsonNodeDataPointMapper<DP extends DataPoint> extends DataPointMapper<DP,JsonNode> {


}
