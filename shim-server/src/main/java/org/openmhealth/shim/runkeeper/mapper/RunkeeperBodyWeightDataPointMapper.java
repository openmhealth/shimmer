package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.DataPoint;

import java.util.Optional;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.WeightSetFeed+json responses to {@link
 * BodyWeight} objects.
 *
 * @author Emerson Farrugia
 * @see <a href="http://runkeeper.com/developer/healthgraph/weight-sets#past">API documentation</a>
 */
public class RunkeeperBodyWeightDataPointMapper extends RunkeeperDataPointMapper<BodyWeight> {

    @Override
    protected Optional<DataPoint<BodyWeight>> asDataPoint(JsonNode itemNode) {

        throw new UnsupportedOperationException("This measure cannot be mapped without time zone information.");
    }
}
