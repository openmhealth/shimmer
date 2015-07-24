package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.FitnessActivityFeed+json responses to {@link
 * PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Danilo Bonilla
 * @see <a href="http://runkeeper.com/developer/healthgraph/fitness-activities#past">API documentation</a>
 */
public class RunKeeperPhysicalActivityDataPointMapper extends RunKeeperDataPointMapper<PhysicalActivity> {

    @Override
    protected Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode itemNode) {

        PhysicalActivity measure = getMeasure(itemNode);
        DataPointHeader header = getDataPointHeader(itemNode, measure);

        return Optional.of(new DataPoint<>(header, measure));
    }

    private PhysicalActivity getMeasure(JsonNode itemNode) {

        String activityName = asRequiredString(itemNode, "type");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        Optional<LocalDateTime> localStartDateTime =
                asOptionalLocalDateTime(itemNode, "start_time", DATE_TIME_FORMATTER);

        // RunKeeper doesn't support fractional time zones
        Optional<Integer> utcOffset = asOptionalInteger(itemNode, "utc_offset");
        Optional<Double> durationInS = asOptionalDouble(itemNode, "duration");

        if (localStartDateTime.isPresent() && utcOffset.isPresent() && durationInS.isPresent()) {

            OffsetDateTime startDateTime = localStartDateTime.get().atOffset(ZoneOffset.ofHours(utcOffset.get()));
            DurationUnitValue duration = new DurationUnitValue(SECOND, durationInS.get());

            builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime, duration));
        }

        asOptionalDouble(itemNode, "total_distance")
                .ifPresent(distanceInM -> builder.setDistance(new LengthUnitValue(METER, distanceInM)));

        return builder.build();
    }




}
