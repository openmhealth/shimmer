package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * A mapper from RunKeeper HealthGraph API application/vnd.com.runkeeper.FitnessActivityFeed+json responses to {@link
 * CaloriesBurned} objects.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public class RunkeeperCaloriesBurnedDataPointMapper extends RunkeeperDataPointMapper<CaloriesBurned> {


    @Override
    protected Optional<DataPoint<CaloriesBurned>> asDataPoint(JsonNode itemNode) {

        Optional<CaloriesBurned> caloriesBurned = getMeasure(itemNode);

        if (caloriesBurned.isPresent()) {
            return Optional
                    .of(new DataPoint<>(getDataPointHeader(itemNode, caloriesBurned.get()), caloriesBurned.get()));
        }
        else {
            return Optional.empty(); // return empty if there was no calories information to generate a datapoint
        }

    }

    private Optional<CaloriesBurned> getMeasure(JsonNode itemNode) {

        Optional<Double> calorieValue = asOptionalDouble(itemNode, "total_calories");
        if (!calorieValue.isPresent()) {  // Not all activity datapoints have the "total_calories" property
            return Optional.empty();
        }
        CaloriesBurned.Builder caloriesBurnedBuilder =
                new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE, calorieValue.get()));

        setEffectiveTimeframeIfPresent(itemNode, caloriesBurnedBuilder);

        Optional<String> activityType = asOptionalString(itemNode, "type");
        activityType.ifPresent(at -> caloriesBurnedBuilder.setActivityName(at));

        return Optional.of(caloriesBurnedBuilder.build());

    }
}
