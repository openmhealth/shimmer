package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DurationUnit;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.LengthUnitValue;
import org.openmhealth.schema.domain.omh.PhysicalActivity;
import org.openmhealth.schema.domain.omh.PhysicalActivity.SelfReportedIntensity;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.time.Instant.ofEpochSecond;
import static java.time.OffsetDateTime.ofInstant;
import static org.openmhealth.schema.domain.omh.LengthUnit.METER;
import static org.openmhealth.schema.domain.omh.PhysicalActivity.SelfReportedIntensity.*;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofEndDateTimeAndDuration;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Jawbone UP API /workouts responses to {@link PhysicalActivity} objects.
 *
 * @author Emerson Farrugia
 * @author Danilo Bonilla
 * @see <a href="https://jawbone.com/up/developer/endpoints/workouts">API documentation</a>
 */
public class JawbonePhysicalActivityDataPointMapper extends JawboneDataPointMapper<PhysicalActivity> {

    private Map<Integer, String> activityNameByWorkoutType = new HashMap<>();


    public JawbonePhysicalActivityDataPointMapper() {

        // initialize activity names
        activityNameByWorkoutType.put(1, "walk");
        activityNameByWorkoutType.put(2, "run");
        activityNameByWorkoutType.put(3, "lift weights");
        activityNameByWorkoutType.put(4, "cross train");
        activityNameByWorkoutType.put(5, "nike training");
        activityNameByWorkoutType.put(6, "yoga");
        activityNameByWorkoutType.put(7, "pilates");
        activityNameByWorkoutType.put(8, "body weight exercise");
        activityNameByWorkoutType.put(9, "crossfit");
        activityNameByWorkoutType.put(10, "p90x");
        activityNameByWorkoutType.put(11, "zumba");
        activityNameByWorkoutType.put(12, "trx");
        activityNameByWorkoutType.put(13, "swim");
        activityNameByWorkoutType.put(14, "bike");
        activityNameByWorkoutType.put(15, "elliptical");
        activityNameByWorkoutType.put(16, "bar method");
        activityNameByWorkoutType.put(17, "kinect exercises");
        activityNameByWorkoutType.put(18, "tennis");
        activityNameByWorkoutType.put(19, "basketball");
        activityNameByWorkoutType.put(20, "golf");
        activityNameByWorkoutType.put(21, "soccer");
        activityNameByWorkoutType.put(22, "ski snowboard");
        activityNameByWorkoutType.put(23, "dance");
        activityNameByWorkoutType.put(24, "hike");
        activityNameByWorkoutType.put(25, "cross country skiing");
        activityNameByWorkoutType.put(26, "stationary bike");
        activityNameByWorkoutType.put(27, "cardio");
        activityNameByWorkoutType.put(28, "game");
        activityNameByWorkoutType.put(29, "other");
    }

    @Override
    protected Optional<PhysicalActivity> getMeasure(JsonNode workoutNode) {
        checkNotNull(workoutNode);

        // assume that the title and workout type are optional since the documentation isn't clear
        Optional<String> title = asOptionalString(workoutNode, "title");
        Optional<Integer> workoutType = asOptionalInteger(workoutNode, "sub_type");

        String activityName = getActivityName(title.orElse(null), workoutType.orElse(null));

        PhysicalActivity.Builder builder = new PhysicalActivity.Builder(activityName);

        asOptionalInteger(workoutNode, "details.meters")
                .ifPresent(distance -> builder.setDistance(new LengthUnitValue(METER, distance)));

        Optional<Long> endTimestamp = asOptionalLong(workoutNode, "time_completed");
        Optional<Long> durationInSec = asOptionalLong(workoutNode, "details.time");
        Optional<ZoneId> timeZoneId = asOptionalZoneId(workoutNode, "details.tz");

        if (endTimestamp.isPresent() && durationInSec.isPresent() && timeZoneId.isPresent()) {
            DurationUnitValue durationUnitValue = new DurationUnitValue(DurationUnit.SECOND, durationInSec.get());
            OffsetDateTime endDateTime = ofInstant(ofEpochSecond(endTimestamp.get()), JawboneDataPointMapper.getTimeZoneForTimestamp(workoutNode,endTimestamp.get()));
            builder.setEffectiveTimeFrame(ofEndDateTimeAndDuration(endDateTime, durationUnitValue));
        }

        asOptionalInteger(workoutNode, "details.intensity")
                .ifPresent(intensity -> builder.setReportedActivityIntensity(asSelfReportedIntensity(intensity)));

        return Optional.of(builder.build());
    }

    /**
     * TODO confirm that we want to make titles trump types
     * @param title the title of the workout, if any
     * @param workoutType the type of the workout, if specified
     * @return the name of the activity
     */
    public String getActivityName(@Nullable String title, @Nullable Integer workoutType) {

        if (title != null) {
            return title;
        }

        if (workoutType == null) {
            return "workout";
        }

        String description = activityNameByWorkoutType.get(workoutType);

        if (description.equals("other")) {
            return "workout";
        }

        return description;
    }

    /**
     * @param intensityValue the workout intensity value in the payload
     * @return the corresponding {@link SelfReportedIntensity}
     */
    public SelfReportedIntensity asSelfReportedIntensity(int intensityValue) {

        switch (intensityValue) {
            case 1:
                return LIGHT;
            case 2:
            case 3:
                return MODERATE;
            case 4:
            case 5:
                return VIGOROUS;
            default:
                throw new IllegalArgumentException(format("The intensity value '%d' isn't supported.", intensityValue));
        }
    }

    @Override
    protected boolean isSensed(JsonNode workoutNode){
        Optional<Integer> steps = asOptionalInteger(workoutNode, "details.steps");

        if (steps.isPresent() && steps.get() > 0) {
            return true;
        }
        return false;
    }
}
