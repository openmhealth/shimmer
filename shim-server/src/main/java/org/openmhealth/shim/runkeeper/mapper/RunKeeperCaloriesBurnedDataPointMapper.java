package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.CaloriesBurned;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.KcalUnit;
import org.openmhealth.schema.domain.omh.KcalUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredDouble;


/**
 * @author Chris Schaefbauer
 */
public class RunKeeperCaloriesBurnedDataPointMapper extends RunKeeperDataPointMapper<CaloriesBurned>{


    @Override
    protected Optional<DataPoint<CaloriesBurned>> asDataPoint(JsonNode itemNode) {
        CaloriesBurned caloriesBurned = getMeasure(itemNode);
        return Optional.of(new DataPoint<>(getDataPointHeader(itemNode,caloriesBurned), caloriesBurned));
    }

    private CaloriesBurned getMeasure(JsonNode itemNode) {
        double calorieValue = asRequiredDouble(itemNode, "total_calories");
        CaloriesBurned.Builder caloriesBurnedBuilder = new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE,calorieValue));

        setEffectiveTimeframeIfPresent(itemNode, caloriesBurnedBuilder);

        Optional<String> activityType = asOptionalString(itemNode, "type");
        activityType.ifPresent(at->caloriesBurnedBuilder.setActivityName(at));

        return caloriesBurnedBuilder.build();
    }
}
