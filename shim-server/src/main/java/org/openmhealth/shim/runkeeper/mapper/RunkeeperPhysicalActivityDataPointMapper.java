package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.FitnessActivityFeed+json responses to {@link
 * PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Danilo Bonilla
 * @see <a href="http://runkeeper.com/developer/healthgraph/fitness-activities#past">API documentation</a>
 */
public class RunkeeperPhysicalActivityDataPointMapper extends RunkeeperDataPointMapper<PhysicalActivity> {

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode itemNode) {

        PhysicalActivity measure = getMeasure(itemNode);
        DataPointHeader header = getDataPointHeader(itemNode, measure);

        return Optional.of(new DataPoint<>(header, measure));
    }

    private PhysicalActivity getMeasure(JsonNode itemNode) {

        String activityName = asRequiredString(itemNode, "type");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        setEffectiveTimeFrameIfPresent(itemNode, builder);

        asOptionalDouble(itemNode, "total_distance")
                .ifPresent(distanceInM -> builder.setDistance(new LengthUnitValue(METER, distanceInM)));

        return builder.build();
    }
}
