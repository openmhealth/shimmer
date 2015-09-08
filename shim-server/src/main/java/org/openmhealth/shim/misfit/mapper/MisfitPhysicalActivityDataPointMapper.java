package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.schema.domain.omh.PhysicalActivity;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.LengthUnit.MILE;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Misfit Resource API /activity/sessions responses to {@link PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Eric Jain
 * @see <a href="https://build.misfit.com/docs/references#APIReferences-Session">API documentation</a>
 */
public class MisfitPhysicalActivityDataPointMapper extends MisfitDataPointMapper<PhysicalActivity> {

    @Override
    protected String getListNodeName() {
        return "sessions";
    }

    @Override
    public Optional<DataPoint<PhysicalActivity>> asDataPoint(JsonNode sessionNode) {

        checkNotNull(sessionNode);

        String activityName = asRequiredString(sessionNode, "activityType");

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        Optional<Double> distance = asOptionalDouble(sessionNode, "distance");

        if (distance.isPresent()) {
            builder.setDistance(new LengthUnitValue(MILE, distance.get()));
        }

        Optional<OffsetDateTime> startDateTime = asOptionalOffsetDateTime(sessionNode, "startTime");
        Optional<Double> durationInSec = asOptionalDouble(sessionNode, "duration");

        if (startDateTime.isPresent() && durationInSec.isPresent()) {
            DurationUnitValue durationUnitValue = new DurationUnitValue(SECOND, durationInSec.get());
            builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime.get(), durationUnitValue));
        }

        PhysicalActivity measure = builder.build();

        Optional<String> externalId = asOptionalString(sessionNode, "id");

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId.orElse(null), null));
    }
}
