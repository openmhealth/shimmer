package org.openmhealth.shim.moves.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.schema.domain.omh.PhysicalActivity;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;

/**
 * A mapper that translates responses from the Moves Resource API <code>/user/storyline/daily</code> endpoint into {@link
 * PhysicalActivity} data points.
 *
 * @author Jared Sieling
 * @see <a href="https://dev.moves-app.com/docs/api_storyline">API documentation</a>
 */
public class MovesPhysicalActivityDataPointMapper extends MovesDataPointMapper<PhysicalActivity>{

    /**
     * Override because the the day-to-dataPoint relationship isn't one-to-one.
     */
    @Override
    public List<DataPoint<PhysicalActivity>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode listNode = responseNodes.get(0);

        List<DataPoint<PhysicalActivity>> dataPoints = Lists.newArrayList();

        for (JsonNode listEntryNode : listNode) {
            JsonNode segments = asRequiredNode(listEntryNode, "segments");

            // Filter out segments that are of type 'place' or activity 'transport'.
            for (JsonNode segment : segments) {
                if(segment.get("type").asText().equals("move")) {
                    JsonNode activities = asRequiredNode(segment, "activities");

                    for (JsonNode activity : activities) {
                        if(!activity.get("group").asText().equals("transport")) {
                            asDataPoint(activity).ifPresent(dataPoints::add);
                        }
                    }
                }
            }
        }

        return dataPoints;
    }

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode node) {

        String activityName = asRequiredString(node, "activity");
        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        Optional<Double> distance = asOptionalDouble(node, "distance");

        distance.ifPresent(aDouble -> builder.setDistance(new LengthUnitValue(METER, aDouble)));

        // TODO update JSON utilities
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssx");
        OffsetDateTime dateTime = OffsetDateTime.parse(node.get("startTime").asText(), formatter);
        Optional<OffsetDateTime> startDateTime = Optional.ofNullable(dateTime);

        Optional<Double> durationInSec = asOptionalDouble(node, "duration");

        if (startDateTime.isPresent() && durationInSec.isPresent()) {
            DurationUnitValue durationUnitValue = new DurationUnitValue(SECOND, durationInSec.get());
            builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime.get(), durationUnitValue));
        }

        PhysicalActivity measure = builder.build();

        return Optional.of(newDataPoint(measure, null));
    }
}
