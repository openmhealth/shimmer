package org.openmhealth.shim.runkeeper.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.CaloriesBurned;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.KcalUnit;
import org.openmhealth.schema.domain.omh.KcalUnitValue;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalString;


/**
 * @author Chris Schaefbauer
 */
public class RunKeeperCaloriesBurnedDataPointMapper extends RunKeeperDataPointMapper<CaloriesBurned>{


    @Override
    protected Optional<DataPoint<CaloriesBurned>> asDataPoint(JsonNode itemNode) {
        Optional<CaloriesBurned> caloriesBurned = getMeasure(itemNode);
        if(caloriesBurned.isPresent()){
            return Optional.of(new DataPoint<>(getDataPointHeader(itemNode,caloriesBurned.get()), caloriesBurned.get()));
        }
        else{
            return Optional.empty();
        }

    }

    private Optional<CaloriesBurned> getMeasure(JsonNode itemNode) {
        Optional<Double> calorieValue = asOptionalDouble(itemNode, "total_calories");
        if(!calorieValue.isPresent()){
            return Optional.empty();
        }
        CaloriesBurned.Builder caloriesBurnedBuilder = new CaloriesBurned.Builder(new KcalUnitValue(KcalUnit.KILOCALORIE,calorieValue.get()));

        setEffectiveTimeframeIfPresent(itemNode, caloriesBurnedBuilder);

        Optional<String> activityType = asOptionalString(itemNode, "type");
        activityType.ifPresent(at -> caloriesBurnedBuilder.setActivityName(at));

        return Optional.of(caloriesBurnedBuilder.build());
    }
}
