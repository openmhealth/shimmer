package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.StepCount;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.ZoneOffset.UTC;
import static org.openmhealth.schema.domain.omh.DurationUnit.DAY;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLocalDate;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredLong;


/**
 * A mapper from Misfit Resource API /activity/summary?detail=true responses to {@link StepCount} objects.
 *
 * @author Emerson Farrugia
 * @author Eric Jain
 * @see <a href="https://build.misfit.com/docs/references#APIReferences-Summary">API documentation</a>
 */
public class MisfitStepCountDataPointMapper extends MisfitDataPointMapper<StepCount> {

    @Override
    protected String getListNodeName() {
        return "summary";
    }

    @Override
    public Optional<DataPoint<StepCount>> asDataPoint(JsonNode summaryNode) {

        checkNotNull(summaryNode);

        Long stepCount = asRequiredLong(summaryNode, "steps");

        if (stepCount == 0) {
            return Optional.empty();
        }

        StepCount.Builder builder = new StepCount.Builder(stepCount);

        // this property isn't listed in the table, but does appear in the second Example section where detail is true
        LocalDate localDate = asRequiredLocalDate(summaryNode, "date");

        // FIXME fix the time zone offset once Misfit add it to the API
        OffsetDateTime startDateTime = localDate.atStartOfDay().atOffset(UTC);

        DurationUnitValue durationUnitValue = new DurationUnitValue(DAY, 1);
        builder.setEffectiveTimeFrame(ofStartDateTimeAndDuration(startDateTime, durationUnitValue));

        StepCount measure = builder.build();

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, null, null));
    }
}
